package com.cl.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.cl.annotation.IgnoreAuth;
import com.cl.dao.FeihualingLeaderboardDao;
import com.cl.dao.FeihualingRecordDao;
import com.cl.entity.FeihualingLeaderboardEntity;
import com.cl.entity.FeihualingRecordEntity;
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

    private static final Map<String, List<String>> PLAYER_HISTORY = new LinkedHashMap<>();
    private static final Map<String, Integer> PLAYER_SCORE = new LinkedHashMap<>();
    private static final Map<String, Integer> PLAYER_COMBO = new LinkedHashMap<>();
    private static final Map<String, Integer> PLAYER_ROUNDS = new LinkedHashMap<>();

    // 关键字稀有度系数
    private static final Map<String, Double> KW_RARITY = new LinkedHashMap<>();
    static {
        for (String kw : new String[]{"山","水","花","月","风","云","春","秋","日","人"}) KW_RARITY.put(kw, 1.0);
        for (String kw : new String[]{"雨","雪","夜","红","白","金","玉","柳","心","梦"}) KW_RARITY.put(kw, 1.3);
    }

    /** AI 飞花令 — 验证+对答合一，减少延迟 */
    @IgnoreAuth
    @RequestMapping("/fei-hua-ling")
    public R feiHuaLing(@RequestParam Map<String, Object> params) {
        String keyword = String.valueOf(params.getOrDefault("keyword", ""));
        String userPoem = String.valueOf(params.getOrDefault("userPoem", ""));
        String sessionId = String.valueOf(params.getOrDefault("sessionId", "default"));

        if (!StringUtils.hasText(keyword)) return R.error("请指定关键字");
        if (!StringUtils.hasText(userPoem)) return R.error("请输入诗句");

        List<String> history = PLAYER_HISTORY.computeIfAbsent(sessionId, k -> new ArrayList<>());
        StringBuilder historyStr = new StringBuilder();
        for (String h : history) {
            if (historyStr.length() < 600) historyStr.append(h).append("；");
        }

        // 合并 prompt：验证+对答一次完成
        String prompt = "飞花令关键字：「" + keyword + "」\n"
            + (historyStr.length() > 0 ? "历史诗句：" + historyStr + "\n" : "")
            + "用户诗句：「" + userPoem + "」\n\n"
            + "任务：1)判断用户诗句是否包含「" + keyword + "」且为真实古诗（非杜撰）；2)接一句含「" + keyword + "」的古诗对句。\n"
            + "严格返回JSON：{\"userValid\":true/false,\"reason\":\"简短纠错\",\"aiPoem\":\"对句\",\"source\":\"出处\",\"aiComment\":\"幽默点评\"}。只返回JSON。";

        List<AIChatUtil.Message> msgs = new ArrayList<>();
        msgs.add(new AIChatUtil.Message("system", "你是古诗词飞花令大师，严格按JSON返回。"));
        msgs.add(new AIChatUtil.Message("user", prompt));
        AIChatUtil.ChatResult cr = AIChatUtil.chatWithMessages(msgs, 0.7, 800);
        String resp = cr != null ? cr.getContent() : null;
        System.out.println("[飞花令] 原始(" + (resp != null ? resp.length() : 0) + "): " + (resp != null ? resp.substring(0, Math.min(300, resp.length())) : "NULL"));

        boolean userValid = true;
        String reason = "";
        String aiPoem = "";
        String source = "";
        String aiComment = "";

        if (resp != null && !resp.isEmpty()) {
            try {
                String json = resp.trim();
                int s = json.indexOf('{'), e = json.lastIndexOf('}');
                if (s >= 0 && e > s) json = json.substring(s, e + 1);
                org.json.JSONObject obj = new org.json.JSONObject(json);
                userValid = obj.optBoolean("userValid", true);
                reason = obj.optString("reason", "");
                aiPoem = obj.optString("aiPoem", "");
                source = obj.optString("source", "");
                aiComment = obj.optString("aiComment", "");
            } catch (Exception ex) {
                System.out.println("[飞花令] JSON异常: " + ex.getMessage());
            }
        }

        if (aiPoem.isEmpty()) {
            aiPoem = getFallback(keyword);
            source = getFallbackSource(keyword);
            aiComment = "AI休息中，经典名句顶上～";
        }

        // 计分逻辑
        String uid = sessionId;
        int roundCount = PLAYER_ROUNDS.getOrDefault(uid, 0);
        int currentScore = PLAYER_SCORE.getOrDefault(uid, 0);
        int combo = PLAYER_COMBO.getOrDefault(uid, 0);

        if (userValid) {
            history.add(userPoem);
            history.add(aiPoem);
            if (history.size() > 16) { history.remove(0); history.remove(0); }
            roundCount++;
            combo++;
            double rarity = KW_RARITY.getOrDefault(keyword, 1.0);
            int base = (int) Math.round(10 * rarity);
            int bonus = combo >= 5 ? (int) Math.round(base * 0.2) : 0;
            currentScore += base + bonus;
            PLAYER_ROUNDS.put(uid, roundCount);
            PLAYER_SCORE.put(uid, currentScore);
            PLAYER_COMBO.put(uid, combo);
        } else {
            PLAYER_COMBO.put(uid, 0); // 无效回答重置连击
        }

        // 生成称号
        String title = currentScore < 30 ? "诗词书童" : currentScore < 80 ? "翰林学士" : "一代诗宗";

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userValid", userValid); result.put("reason", reason);
        result.put("aiPoem", aiPoem); result.put("source", source);
        result.put("aiComment", aiComment);
        result.put("roundCount", roundCount); result.put("score", currentScore);
        result.put("combo", combo); result.put("title", title);
        return R.ok().put("data", result);
    }

    private String getFallback(String kw) {
        String[][] db = {
            {"月","床前明月光，疑是地上霜","《静夜思》李白"},
            {"花","花开堪折直须折，莫待无花空折枝","《金缕衣》杜秋娘"},
            {"风","春风得意马蹄疾，一日看尽长安花","《登科后》孟郊"},
            {"云","远上寒山石径斜，白云深处有人家","《山行》杜牧"},
            {"山","会当凌绝顶，一览众山小","《望岳》杜甫"},
            {"水","问渠那得清如许，为有源头活水来","《观书有感》朱熹"},
            {"春","春眠不觉晓，处处闻啼鸟","《春晓》孟浩然"},
            {"秋","一年好景君须记，最是橙黄橘绿时","《赠刘景文》苏轼"},
            {"日","日出江花红胜火，春来江水绿如蓝","《忆江南》白居易"},
            {"雨","好雨知时节，当春乃发生","《春夜喜雨》杜甫"},
            {"雪","忽如一夜春风来，千树万树梨花开","《白雪歌送武判官归京》岑参"},
            {"夜","二十四桥明月夜，玉人何处教吹箫","《寄扬州韩绰判官》杜牧"},
            {"人","遥知兄弟登高处，遍插茱萸少一人","《九月九日忆山东兄弟》王维"},
            {"心","人生自古谁无死，留取丹心照汗青","《过零丁洋》文天祥"},
            {"梦","夜阑卧听风吹雨，铁马冰河入梦来","《十一月四日风雨大作》陆游"},
            {"红","日出江花红胜火，春来江水绿如蓝","《忆江南》白居易"},
            {"白","朝辞白帝彩云间，千里江陵一日还","《早发白帝城》李白"},
            {"金","劝君莫惜金缕衣，劝君惜取少年时","《金缕衣》杜秋娘"},
            {"玉","碧玉妆成一树高，万条垂下绿丝绦","《咏柳》贺知章"},
            {"柳","沾衣欲湿杏花雨，吹面不寒杨柳风","《绝句》志南"},
        };
        for (String[] row : db) if (row[0].equals(kw)) return row[1];
        return "春风又绿江南岸，明月何时照我还";
    }
    private String getFallbackSource(String kw) {
        String[][] db = {{"月","《静夜思》李白"},{"花","《金缕衣》杜秋娘"},{"风","《登科后》孟郊"},{"云","《山行》杜牧"},{"山","《望岳》杜甫"},{"水","《观书有感》朱熹"},{"春","《春晓》孟浩然"},{"秋","《赠刘景文》苏轼"},{"日","《忆江南》白居易"},{"雨","《春夜喜雨》杜甫"},{"雪","《白雪歌送武判官归京》岑参"},{"夜","《寄扬州韩绰判官》杜牧"},{"人","《九月九日忆山东兄弟》王维"},{"心","《过零丁洋》文天祥"},{"梦","《十一月四日风雨大作》陆游"},{"红","《忆江南》白居易"},{"白","《早发白帝城》李白"},{"金","《金缕衣》杜秋娘"},{"玉","《咏柳》贺知章"},{"柳","《绝句》志南"}};
        for (String[] row : db) if (row[0].equals(kw)) return row[1];
        return "《泊船瓜洲》王安石";
    }

    /** 保存本局成绩 + 更新排行榜 */
    @IgnoreAuth @RequestMapping("/saveRecord")
    public R saveRecord(@RequestParam Map<String, Object> params, HttpServletRequest req) {
        String uid = String.valueOf(params.getOrDefault("sessionId", "default"));
        int score = Integer.parseInt(String.valueOf(params.getOrDefault("score", "0")));
        int rounds = Integer.parseInt(String.valueOf(params.getOrDefault("rounds", "0")));
        int maxCombo = Integer.parseInt(String.valueOf(params.getOrDefault("maxCombo", "0")));
        String keyword = String.valueOf(params.getOrDefault("keyword", ""));
        String uname = String.valueOf(req.getSession().getAttribute("username"));
        if ("null".equals(uname)) uname = uid;

        FeihualingRecordEntity rec = new FeihualingRecordEntity();
        rec.setUserId(uid); rec.setUsername(uname); rec.setKeyword(keyword);
        rec.setRounds(rounds); rec.setScore(score); rec.setMaxCombo(maxCombo);
        rec.setAddtime(new Date());
        fhlRecordDao.insert(rec);

        // 更新排行榜
        FeihualingLeaderboardEntity lb = fhlLeaderboardDao.selectById(uid);
        if (lb == null) {
            lb = new FeihualingLeaderboardEntity(); lb.setUserId(uid); lb.setUsername(uname);
            lb.setTotalGames(0); lb.setTotalWins(0); lb.setTotalRounds(0);
        }
        lb.setTotalGames(lb.getTotalGames() + 1);
        lb.setTotalRounds(lb.getTotalRounds() + rounds);
        if (score >= 50) lb.setTotalWins(lb.getTotalWins() + 1);
        if (score > lb.getMaxScore()) lb.setMaxScore(score);
        lb.setTitle(score < 30 ? "诗词书童" : score < 80 ? "翰林学士" : "一代诗宗");
        lb.setUpdateTime(new Date());
        if (fhlLeaderboardDao.selectById(uid) != null) fhlLeaderboardDao.updateById(lb);
        else fhlLeaderboardDao.insert(lb);

        // 清除游戏状态
        PLAYER_HISTORY.remove(uid); PLAYER_SCORE.remove(uid);
        PLAYER_COMBO.remove(uid); PLAYER_ROUNDS.remove(uid);
        return R.ok();
    }

    /** 排行榜 Top 50 */
    @IgnoreAuth @RequestMapping("/leaderboard")
    public R leaderboard() {
        EntityWrapper<FeihualingLeaderboardEntity> ew = new EntityWrapper<>();
        ew.orderBy("max_score", false).last("LIMIT 50");
        List<FeihualingLeaderboardEntity> list = fhlLeaderboardDao.selectList(ew);
        return R.ok().put("data", list);
    }

    /** 排名百分比 */
    @IgnoreAuth @RequestMapping("/rank")
    public R rank(@RequestParam int score) {
        int total = fhlLeaderboardDao.selectCount(new EntityWrapper<>());
        EntityWrapper<FeihualingLeaderboardEntity> ew = new EntityWrapper<>();
        ew.gt("max_score", (long) score);
        int higher = fhlLeaderboardDao.selectCount(ew);
        double pct = total > 0 ? Math.round((1.0 - (double) higher / total) * 100) : 100;
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("percentage", (int) pct); r.put("total", total);
        return R.ok().put("data", r);
    }

    /** 用户飞花令档案 */
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

    @IgnoreAuth
    @RequestMapping("/reset")
    public R reset(@RequestParam String sessionId) {
        PLAYER_HISTORY.remove(sessionId);
        PLAYER_SCORE.remove(sessionId);
        PLAYER_COMBO.remove(sessionId);
        PLAYER_ROUNDS.remove(sessionId);
        return R.ok();
    }

}
