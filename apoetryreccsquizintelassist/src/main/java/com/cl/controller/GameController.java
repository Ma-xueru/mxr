package com.cl.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.cl.annotation.IgnoreAuth;
import com.cl.dao.FeihualingLeaderboardDao;
import com.cl.dao.FeihualingRecordDao;
import com.cl.dao.FollowreadRecordDao;
import com.cl.dao.QuizRecordDao;
import com.cl.entity.FeihualingLeaderboardEntity;
import com.cl.entity.FeihualingRecordEntity;
import com.cl.entity.FollowreadRecordEntity;
import com.cl.entity.QuizRecordEntity;
import com.cl.utils.AIChatUtil;
import com.cl.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/game")
public class GameController {

    @Autowired private FeihualingRecordDao fhlRecordDao;
    @Autowired private FeihualingLeaderboardDao fhlLeaderboardDao;
    @Autowired private FollowreadRecordDao followreadRecordDao;
    @Autowired private QuizRecordDao quizRecordDao;

    // ========== 游戏状态 ==========
    private static final Map<String, GameState> STATES = new LinkedHashMap<>();

    static class GameState {
        String keyword; int lives = 3; int roundCount; int score; int combo; int maxCombo;
        long startTime; long lastRoundTime;
        Set<String> usedPoems = new LinkedHashSet<>();
        List<String> chatHistory = new ArrayList<>();
    }

    // 关键字稀有度系数
    private static final Map<String, Double> KW_RARITY = new LinkedHashMap<>();
    static {
        for (String kw : new String[]{"山","水","花","月","风","云","春","秋","日","人"}) KW_RARITY.put(kw, 1.0);
        for (String kw : new String[]{"雨","雪","夜","红","白","金","玉","柳","心","梦"}) KW_RARITY.put(kw, 1.3);
    }

    // AI 裁判 System Prompt — 基础版，运行时根据characterId动态注入
    private static final String JUDGE_PROMPT_BASE = "你是中国古诗词大赛的严格裁判。请验证用户输入的诗词句。\n" +
        "判罚准则：\n1. 必须是中国真实古诗词名句（不能是用户自己编的或现代白话）\n" +
        "2. 必须准确包含指定的关键字\n3. 如果用户输入\"你好\"、\"emm\"、\"c\"等无意义内容，判定为非法输入\n" +
        "4. 如果用户输错了字（如\"我\"应为\"卧\"），判定为无效并指出正确写法\n\n" +
        "即使输入无效，你也要接一句含关键字的真实古诗作为示范。\n" +
        "给一句幽默有趣的儿童化点评（如点评大白鹅抢镜、诗人太有才等）。\n" +
        "严格返回JSON：{\"isValid\":true/false,\"reason\":\"简短原因\",\"aiPoem\":\"对句\",\"source\":\"出处\",\"aiComment\":\"幽默点评\"}。只返回JSON。";

    // ========== 核心 API ==========

    /** 初始化新游戏 */
    @IgnoreAuth @RequestMapping("/init")
    public R init(@RequestParam String keyword, @RequestParam String sessionId) {
        GameState gs = new GameState();
        gs.keyword = keyword; gs.startTime = System.currentTimeMillis(); gs.lastRoundTime = gs.startTime;
        STATES.put(sessionId, gs);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("lives", 3); r.put("keyword", keyword);
        return R.ok().put("data", r);
    }

    /** 飞花令 — 多级校验 + AI对句(含错误批注) + 计分 */
    @IgnoreAuth @RequestMapping("/fei-hua-ling")
    public R feiHuaLing(@RequestParam Map<String, Object> params) {
        String keyword = String.valueOf(params.getOrDefault("keyword", ""));
        String userPoem = String.valueOf(params.getOrDefault("userPoem", "")).trim().replaceAll("\\s+", "");
        String sessionId = String.valueOf(params.getOrDefault("sessionId", "default"));

        if (!StringUtils.hasText(keyword)) return R.error("请指定关键字");
        if (!StringUtils.hasText(userPoem)) return R.error("请输入诗句");

        GameState gs = STATES.computeIfAbsent(sessionId, k -> { GameState g = new GameState(); g.keyword = keyword; g.startTime = System.currentTimeMillis(); g.lastRoundTime = g.startTime; return g; });
        boolean valid = true;
        String failReason = "";

        // ===== Layer 1: 非中文 =====
        if (userPoem.length() < 2 || !userPoem.matches(".*[\\u4e00-\\u9fa5].*")) {
            gs.lives--; valid = false; failReason = "请输入中文古诗词句";
        }
        // ===== Layer 2: 关键字 =====
        else if (!userPoem.contains(keyword)) {
            gs.lives--; gs.combo = 0; valid = false; failReason = "诗句不包含关键字「" + keyword + "」";
        }
        // ===== Layer 3: 重复 =====
        else {
            String poemKey = userPoem.replaceAll("[，。！？、；：\\s]", "");
            if (gs.usedPoems.contains(poemKey)) {
                gs.lives--; gs.combo = 0; valid = false; failReason = "这句诗已经用过啦，请换一句～";
            }
        }

        // ===== 统一调用AI生成对句和批注(正确/错误都有) =====
        StringBuilder histStr = new StringBuilder();
        for (String h : gs.chatHistory) { if (histStr.length() < 500) histStr.append(h).append("；"); }
        String prompt = "关键字：「" + keyword + "」\n历史：" + histStr + "\n用户输入：「" + userPoem + "」"
            + (valid ? "\naiPoem必须包含「" + keyword + "」字，必须是真实的古诗词句。" : "\n用户回答有误(" + failReason + ")，请用幽默语气指出。aiPoem必须包含「" + keyword + "」字，必须是真实的古诗词句。");

        String characterId = String.valueOf(params.getOrDefault("characterId", ""));
        String judgePrompt = com.cl.utils.CharacterPromptUtil.feihualingPrompt(characterId);

        String aiPoem = getFallback(keyword), source = getFallbackSource(keyword), aiComment = failReason;
        try {
            List<AIChatUtil.Message> msgs = new ArrayList<>();
            msgs.add(new AIChatUtil.Message("system", judgePrompt));
            msgs.add(new AIChatUtil.Message("user", prompt));
            AIChatUtil.ChatResult cr = AIChatUtil.chatWithMessages(msgs, 0.5, 600);
            String resp = cr != null ? cr.getContent() : null;
            if (resp != null && !resp.isEmpty()) {
                String json = resp.trim(); int s = json.indexOf('{'), e = json.lastIndexOf('}');
                if (s >= 0 && e > s) json = json.substring(s, e + 1);
                org.json.JSONObject obj = new org.json.JSONObject(json);
                aiPoem = obj.optString("aiPoem", aiPoem);
                source = obj.optString("source", source);
                aiComment = obj.optString("aiComment", aiComment);
            }
        } catch (Exception ex) { aiComment = failReason.isEmpty() ? aiComment : failReason; }
        if (aiPoem.isEmpty() || !aiPoem.contains(keyword)) { aiPoem = getFallback(keyword); source = getFallbackSource(keyword); }

        // ===== 计分：只有答对才+10 =====
        if (valid) {
            String poemKey = userPoem.replaceAll("[，。！？、；：\\s]", "");
            gs.usedPoems.add(poemKey);
            gs.chatHistory.add(userPoem); gs.chatHistory.add(aiPoem);
            if (gs.chatHistory.size() > 16) { gs.chatHistory.remove(0); gs.chatHistory.remove(0); }
            gs.roundCount++; gs.combo++;
            if (gs.combo > gs.maxCombo) gs.maxCombo = gs.combo;
            gs.score += 10;
        }
        gs.lastRoundTime = System.currentTimeMillis();

        Map<String, Object> r = buildResult(gs, keyword, valid, valid ? "" : failReason, aiPoem, source, aiComment, aiPoem, source);
        if (gs.lives <= 0) r.put("gameOver", true);
        return R.ok().put("data", r);
    }

    private Map<String, Object> buildResult(GameState gs, String kw, boolean valid, String reason,
            String aiPoem, String source, String aiComment, String fbPoem, String fbSource) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("isValid", valid); r.put("reason", reason);
        r.put("aiPoem", aiPoem.isEmpty() ? fbPoem : aiPoem);
        r.put("source", source.isEmpty() ? fbSource : source);
        r.put("aiComment", aiComment != null ? aiComment : "");
        r.put("lives", gs.lives); r.put("roundCount", gs.roundCount);
        r.put("score", gs.score); r.put("combo", gs.combo);
        return r;
    }

    /** 超时扣除生命值 */
    @IgnoreAuth @RequestMapping("/timeout")
    public R timeout(@RequestParam String sessionId) {
        GameState gs = STATES.get(sessionId);
        if (gs == null) return R.ok();
        gs.lives--; gs.combo = 0;
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("lives", gs.lives); r.put("score", gs.score);
        r.put("roundCount", gs.roundCount); r.put("aiPoem", getFallback(gs.keyword));
        r.put("source", getFallbackSource(gs.keyword));
        r.put("aiComment", "超时了，下次反应快点哦⏰");
        r.put("reason", "超时未作答");
        if (gs.lives <= 0) r.put("gameOver", true);
        return R.ok().put("data", r);
    }

    /** 手动结算 */
    @IgnoreAuth @RequestMapping("/settlement")
    public R settlement(@RequestParam String sessionId, HttpServletRequest req) {
        GameState gs = STATES.get(sessionId);
        if (gs == null) return R.error("游戏不存在");
        long duration = (System.currentTimeMillis() - gs.startTime) / 1000;
        int total = fhlLeaderboardDao.selectCount(new EntityWrapper<>());
        EntityWrapper<FeihualingLeaderboardEntity> ew = new EntityWrapper<>();
        ew.gt("max_score", (long) gs.score);
        int higher = fhlLeaderboardDao.selectCount(ew);
        int rankPct = total > 0 ? (int) Math.round((1.0 - (double) higher / total) * 100) : 100;
        String title = gs.score < 30 ? "诗词书童" : gs.score < 80 ? "翰林学士" : "一代诗宗";

        // 保存记录
        FeihualingRecordEntity rec = new FeihualingRecordEntity();
        rec.setUserId(sessionId); rec.setUsername(sessionId); rec.setKeyword(gs.keyword);
        rec.setRounds(gs.roundCount); rec.setScore(gs.score); rec.setMaxCombo(gs.maxCombo);
        rec.setAddtime(new Date());
        fhlRecordDao.insert(rec);

        // 更新排行榜
        FeihualingLeaderboardEntity lb = fhlLeaderboardDao.selectById(sessionId);
        if (lb == null) { lb = new FeihualingLeaderboardEntity(); lb.setUserId(sessionId); lb.setUsername(sessionId); lb.setTotalGames(0); lb.setTotalWins(0); lb.setTotalRounds(0); }
        lb.setTotalGames(lb.getTotalGames() + 1);
        lb.setTotalRounds(lb.getTotalRounds() + gs.roundCount);
        if (gs.score >= 50) lb.setTotalWins(lb.getTotalWins() + 1);
        if (gs.score > lb.getMaxScore()) lb.setMaxScore(gs.score);
        lb.setTitle(title); lb.setUpdateTime(new Date());
        if (fhlLeaderboardDao.selectById(sessionId) != null) fhlLeaderboardDao.updateById(lb);
        else fhlLeaderboardDao.insert(lb);

        STATES.remove(sessionId);

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("score", gs.score); r.put("rounds", gs.roundCount); r.put("maxCombo", gs.maxCombo);
        r.put("duration", (int) duration); r.put("rankPct", rankPct); r.put("title", title);
        r.put("keyword", gs.keyword);
        return R.ok().put("data", r);
    }

    /** 排行榜 Top 50 */
    @IgnoreAuth @RequestMapping("/leaderboard")
    public R leaderboard() {
        EntityWrapper<FeihualingLeaderboardEntity> ew = new EntityWrapper<>();
        ew.orderBy("max_score", false).last("LIMIT 50");
        return R.ok().put("data", fhlLeaderboardDao.selectList(ew));
    }

    /** 排名百分比 */
    @IgnoreAuth @RequestMapping("/rank")
    public R rank(@RequestParam int score) {
        int total = fhlLeaderboardDao.selectCount(new EntityWrapper<>());
        EntityWrapper<FeihualingLeaderboardEntity> ew = new EntityWrapper<>();
        ew.gt("max_score", (long) score);
        int higher = fhlLeaderboardDao.selectCount(ew);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("percentage", total > 0 ? (int) Math.round((1.0 - (double) higher / total) * 100) : 100);
        r.put("total", total);
        return R.ok().put("data", r);
    }

    @IgnoreAuth @RequestMapping("/profile")
    public R profile(HttpServletRequest req) {
        String uid = String.valueOf(req.getSession().getAttribute("username"));
        if ("null".equals(uid)) uid = "default";
        FeihualingLeaderboardEntity lb = fhlLeaderboardDao.selectById(uid);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("maxScore", lb != null ? lb.getMaxScore() : 0);
        r.put("title", lb != null ? lb.getTitle() : "诗词书童");
        r.put("totalGames", lb != null ? lb.getTotalGames() : 0);
        r.put("totalWins", lb != null ? lb.getTotalWins() : 0);
        return R.ok().put("data", r);
    }

    /** 温故知新 — AI基于14天学习记录出题 */
    @IgnoreAuth @RequestMapping("/history-review")
    public R historyReview(HttpServletRequest request) {
        long fourteenDaysAgo = System.currentTimeMillis() - 14 * 86400000L;
        EntityWrapper<FollowreadRecordEntity> fw = new EntityWrapper<>();
        fw.ge("addtime", new java.util.Date(fourteenDaysAgo));
        List<FollowreadRecordEntity> followList = followreadRecordDao.selectList(fw);
        EntityWrapper<QuizRecordEntity> qw = new EntityWrapper<>();
        qw.ge("addtime", new java.util.Date(fourteenDaysAgo));
        List<QuizRecordEntity> quizList = quizRecordDao.selectList(qw);

        Map<Long, Map<String, Object>> poemMap = new LinkedHashMap<>();
        for (FollowreadRecordEntity r : followList) {
            poemMap.computeIfAbsent(r.getCourseid(), k -> {
                Map<String, Object> m = new LinkedHashMap<>(); m.put("courseTitle", r.getCoursetitle()); m.put("types", new ArrayList<String>()); return m;
            });
            ((List<String>)poemMap.get(r.getCourseid()).get("types")).add("recitation");
        }
        for (QuizRecordEntity r : quizList) {
            poemMap.computeIfAbsent(r.getCourseid(), k -> {
                Map<String, Object> m = new LinkedHashMap<>(); m.put("courseTitle", r.getCoursetitle()); m.put("types", new ArrayList<String>()); return m;
            });
            String type = r.getScore() != null && r.getScore() < 80 ? "comprehension" : "quiz";
            ((List<String>)poemMap.get(r.getCourseid()).get("types")).add(type);
        }
        if (poemMap.isEmpty()) {
            // 无记录时出通用复习题
            String fallbackPrompt = "请出10道小学古诗词通用复习题，涵盖字词释义(3题)、诗句理解(3题)、文学常识(2题)、诵读节奏(2题)。返回JSON：[{\"question\":\"题?\",\"options\":[\"A\",\"B\",\"C\",\"D\"],\"answer\":0,\"analysis\":\"解析\",\"knowledge_tag\":\"标签\"}]。只返回JSON。";
            List<AIChatUtil.Message> msgs2 = new ArrayList<>();
            msgs2.add(new AIChatUtil.Message("system", "你是古诗词教学专家，严格按JSON返回10道题。"));
            msgs2.add(new AIChatUtil.Message("user", fallbackPrompt));
            AIChatUtil.ChatResult cr2 = AIChatUtil.chatWithMessages(msgs2, 0.5, 3500);
            String resp2 = cr2 != null ? cr2.getContent() : null;
            if (resp2 == null || resp2.isEmpty()) return R.error("AI未返回题目");
            return R.ok().put("data", cleanJson(resp2));
        }

        StringBuilder ctx = new StringBuilder();
        for (Map<String, Object> pm : poemMap.values())
            ctx.append("《").append(pm.get("courseTitle")).append("》维度：").append(pm.get("types")).append("；");

        String prompt = "根据学生近14天学习记录出10道题：\n" + ctx + "\n规则：1.标recitation的诗出读音/断句题 2.标comprehension的出意境理解题 3.返回JSON数组：[{\"question\":\"题?\",\"options\":[\"A\",\"B\",\"C\",\"D\"],\"answer\":0,\"analysis\":\"解析\",\"knowledge_tag\":\"标签\"}]。只返回JSON。";
        List<AIChatUtil.Message> msgs = new ArrayList<>();
        msgs.add(new AIChatUtil.Message("system", "你是古诗词教学专家，严格按JSON返回10道题。"));
        msgs.add(new AIChatUtil.Message("user", prompt));
        AIChatUtil.ChatResult cr = AIChatUtil.chatWithMessages(msgs, 0.5, 2500);
        String resp = cr != null ? cr.getContent() : null;
        System.out.println("[温故知新] AI(" + (resp != null ? resp.length() : 0) + ")");
        if (resp == null || resp.isEmpty()) return R.error("AI未返回题目");
        return R.ok().put("data", cleanJson(resp));
    }

    /** 举一反三 — AI基于错题标签匹配同类诗词出题 */
    @IgnoreAuth @RequestMapping("/analogy-training")
    public R analogyTraining() {
        EntityWrapper<QuizRecordEntity> qw = new EntityWrapper<>();
        qw.isNotNull("wrong_list_json").ne("wrong_list_json", "[]").orderBy("addtime", false).last("LIMIT 5");
        List<QuizRecordEntity> quizList = quizRecordDao.selectList(qw);
        if (quizList.isEmpty()) return R.error("暂无错题记录，先做几道题吧～");

        Set<String> tags = new LinkedHashSet<>(), forbidden = new LinkedHashSet<>();
        for (QuizRecordEntity r : quizList) {
            try {
                org.json.JSONArray arr = new org.json.JSONArray(r.getWrongListJson());
                for (int i = 0; i < arr.length(); i++) {
                    String q = arr.getJSONObject(i).optString("question", "");
                    String tag = q.contains("意思") || q.contains("解释") ? "字词释义" : q.contains("情感") || q.contains("意境") ? "意境感悟" : q.contains("诗人") || q.contains("背景") ? "文学常识" : q.contains("读音") || q.contains("断句") || q.contains("节奏") ? "诵读节奏" : q.contains("月") || q.contains("花") ? "意象" : "";
                    if (!tag.isEmpty()) tags.add(tag);
                }
            } catch (Exception e) {}
            if (r.getCoursetitle() != null) forbidden.add(r.getCoursetitle());
        }
        if (tags.isEmpty()) return R.error("暂未识别薄弱标签");

        String prompt = "薄弱标签：" + String.join(",", tags) + "。\n严禁出现：" + String.join(",", forbidden) + "。\n选取2-3首同类但不在禁止列表中的古诗，出10道对比选择题。返回JSON：[{\"question\":\"题?\",\"options\":[\"A\",\"B\",\"C\",\"D\"],\"answer\":0,\"analysis\":\"解析\",\"knowledge_tag\":\"标签\"}]。只返回JSON。";
        List<AIChatUtil.Message> msgs = new ArrayList<>();
        msgs.add(new AIChatUtil.Message("system", "你是古诗词教学专家，擅长类比出题，严格按JSON返回10道题。"));
        msgs.add(new AIChatUtil.Message("user", prompt));
        AIChatUtil.ChatResult cr = AIChatUtil.chatWithMessages(msgs, 0.5, 2500);
        String resp = cr != null ? cr.getContent() : null;
        System.out.println("[举一反三] AI(" + (resp != null ? resp.length() : 0) + ")");
        if (resp == null || resp.isEmpty()) return R.error("AI未返回题目");
        return R.ok().put("data", cleanJson(resp));
    }

    private String cleanJson(String resp) {
        String s = resp.trim();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```").matcher(s);
        if (m.find()) s = m.group(1).trim();
        int start = s.indexOf('['), end = s.lastIndexOf(']');
        if (start >= 0 && end > start) s = s.substring(start, end + 1);
        return s;
    }

    /** 错题本 — 仅返回当前学生自己的错题 */
    @RequestMapping("/wrongbook")
    public R wrongbook(HttpServletRequest request) {
        String tableName = String.valueOf(request.getSession().getAttribute("tableName"));
        String username = String.valueOf(request.getSession().getAttribute("username"));
        EntityWrapper<QuizRecordEntity> ew = new EntityWrapper<>();
        if ("student".equals(tableName)) ew.eq("studentaccount", username);
        ew.isNotNull("wrong_list_json").ne("wrong_list_json", "[]").orderBy("addtime", false);
        List<QuizRecordEntity> list = quizRecordDao.selectList(ew);
        List<Map<String, Object>> wrongList = new ArrayList<>();
        for (QuizRecordEntity r : list) {
            try {
                org.json.JSONArray arr = new org.json.JSONArray(r.getWrongListJson());
                for (int i = 0; i < arr.length(); i++) {
                    org.json.JSONObject w = arr.getJSONObject(i);
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("poemTitle", r.getCoursetitle());
                    item.put("question", w.optString("question"));
                    item.put("options", w.optJSONArray("options") != null ? w.optJSONArray("options").toList() : new ArrayList<>());
                    item.put("answer", w.optInt("answer"));
                    item.put("selected", w.optInt("selected"));
                    item.put("analysis", w.optString("analysis"));
                    wrongList.add(item);
                }
            } catch (Exception e) {}
        }
        return R.ok().put("data", wrongList);
    }

    @IgnoreAuth @RequestMapping("/reset")
    public R reset(@RequestParam String sessionId) { STATES.remove(sessionId); return R.ok(); }

    // ========== Fallback ==========
    private String getFallback(String kw) {
        String[][] db = {{"月","床前明月光，疑是地上霜","《静夜思》李白"},{"花","花开堪折直须折，莫待无花空折枝","《金缕衣》杜秋娘"},{"风","春风得意马蹄疾，一日看尽长安花","《登科后》孟郊"},{"云","远上寒山石径斜，白云深处有人家","《山行》杜牧"},{"山","会当凌绝顶，一览众山小","《望岳》杜甫"},{"水","问渠那得清如许，为有源头活水来","《观书有感》朱熹"},{"春","春眠不觉晓，处处闻啼鸟","《春晓》孟浩然"},{"秋","一年好景君须记，最是橙黄橘绿时","《赠刘景文》苏轼"},{"日","日出江花红胜火，春来江水绿如蓝","《忆江南》白居易"},{"雨","好雨知时节，当春乃发生","《春夜喜雨》杜甫"},{"雪","忽如一夜春风来，千树万树梨花开","《白雪歌送武判官归京》岑参"},{"夜","二十四桥明月夜，玉人何处教吹箫","《寄扬州韩绰判官》杜牧"},{"人","遥知兄弟登高处，遍插茱萸少一人","《九月九日忆山东兄弟》王维"},{"心","人生自古谁无死，留取丹心照汗青","《过零丁洋》文天祥"},{"梦","夜阑卧听风吹雨，铁马冰河入梦来","《十一月四日风雨大作》陆游"},{"红","日出江花红胜火，春来江水绿如蓝","《忆江南》白居易"},{"白","朝辞白帝彩云间，千里江陵一日还","《早发白帝城》李白"},{"金","劝君莫惜金缕衣，劝君惜取少年时","《金缕衣》杜秋娘"},{"玉","碧玉妆成一树高，万条垂下绿丝绦","《咏柳》贺知章"},{"柳","沾衣欲湿杏花雨，吹面不寒杨柳风","《绝句》志南"}};
        for (String[] row : db) if (row[0].equals(kw)) return row[1];
        return "春风又绿江南岸，明月何时照我还";
    }
    private String getFallbackSource(String kw) {
        String[][] db = {{"月","《静夜思》李白"},{"花","《金缕衣》杜秋娘"},{"风","《登科后》孟郊"},{"云","《山行》杜牧"},{"山","《望岳》杜甫"},{"水","《观书有感》朱熹"},{"春","《春晓》孟浩然"},{"秋","《赠刘景文》苏轼"},{"日","《忆江南》白居易"},{"雨","《春夜喜雨》杜甫"},{"雪","《白雪歌送武判官归京》岑参"},{"夜","《寄扬州韩绰判官》杜牧"},{"人","《九月九日忆山东兄弟》王维"},{"心","《过零丁洋》文天祥"},{"梦","《十一月四日风雨大作》陆游"},{"红","《忆江南》白居易"},{"白","《早发白帝城》李白"},{"金","《金缕衣》杜秋娘"},{"玉","《咏柳》贺知章"},{"柳","《绝句》志南"}};
        for (String[] row : db) if (row[0].equals(kw)) return row[1];
        return "《泊船瓜洲》王安石";
    }
}
