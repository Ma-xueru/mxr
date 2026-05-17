<template>
  <div>
    <el-dialog v-model="formVisible" :title="formTitle" width="85%" destroy-on-close :fullscreen='false' class="task_dialog" :close-on-click-modal="false">
      <div class="dialog_intro">
        <div class="intro_badge">🤖 AI 智能出题</div>
        <div class="intro_title">{{ form.tasktitle || '选择古诗，让 DeepSeek 帮你生成测验题' }}</div>
        <div class="intro_desc">选班级、选古诗、定题量 → AI 秒出题 → 老师审查修改 → 一键级联发布给全班学生</div>
      </div>

      <el-form class="formModel_form" ref="formRef" :model="form" label-width="100px">
        <!-- 发布表单 -->
        <div class="form_section">
          <div class="section_title">测验配置</div>
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="测验标题">
                <el-input v-model="form.tasktitle" placeholder="例如：第一单元古诗测验" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="发布班级">
                <el-select v-model="selectedClasses" placeholder="选择管辖班级（可多选）" multiple collapse-tags style="width:100%">
                  <el-option v-for="c in classOptions" :key="c" :label="c" :value="c" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="指定古诗">
                <el-select v-model="selectedGrade" placeholder="先选年级" style="width:100%;margin-bottom:8px" @change="gradeChange">
                  <el-option v-for="g in gradeOptions" :key="g" :label="g" :value="g" />
                </el-select>
                <el-select v-model="selectedPoems" placeholder="选择古诗（可多选）" multiple collapse-tags style="width:100%" :disabled="!selectedGrade">
                  <el-option v-for="p in filteredPoems" :key="p.id" :label="p.coursetitle" :value="p.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="题目数量">
                <el-input-number v-model="questionCount" :min="1" :max="30" />
              </el-form-item>
            </el-col>
            <el-col :span="18">
              <el-form-item label=" ">
                <el-button type="warning" :loading="generating" @click="generateQuiz" :disabled="!selectedPoems.length || !form.tasktitle">
                  🤖 {{ generating ? 'DeepSeek 出题中...' : '触发 DeepSeek 智能生成题目' }}
                </el-button>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 题目审查工作区 -->
        <div class="form_section" v-if="questions.length">
          <div class="section_title">📝 题目审查工作区（{{ questions.length }} 题，可修改/删除）</div>
          <div class="question_cards">
            <el-card v-for="(q, idx) in questions" :key="idx" class="question_card" shadow="hover">
              <template #header>
                <div class="card_header">
                  <span class="card_idx">第 {{ idx + 1 }} 题</span>
                  <el-tag size="small">{{ q.questionType == 2 ? '填空题' : '单选题' }}</el-tag>
                  <el-button type="danger" size="small" circle @click="removeQuestion(idx)" style="margin-left:auto">✕</el-button>
                </div>
              </template>
              <el-row :gutter="12">
                <el-col :span="24">
                  <el-form-item label="题干"><el-input v-model="q.content" type="textarea" :rows="2" /></el-form-item>
                </el-col>
                <el-col :span="24" v-if="q.optionsJson">
                  <el-form-item label="选项">
                    <div class="option_grid">
                      <template v-for="(val, key) in parseOptions(q.optionsJson)" :key="key">
                        <el-input v-model="parseOptions(q.optionsJson)[key]" :placeholder="'选项 ' + key">
                          <template #prepend>{{ key }}</template>
                        </el-input>
                      </template>
                    </div>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="正确答案"><el-input v-model="q.correctAnswer" /></el-form-item>
                </el-col>
                <el-col :span="16">
                  <el-form-item label="解析"><el-input v-model="q.analysis" /></el-form-item>
                </el-col>
              </el-row>
            </el-card>
          </div>

          <!-- 发布按钮 -->
          <div style="text-align:center;margin-top:20px">
            <el-button type="success" size="large" :loading="releasing" @click="releaseQuiz" :disabled="!selectedClasses.length">
              🚀 审查通过，一键发布给学生
            </el-button>
            <div style="color:#9a8d73;font-size:12px;margin-top:6px">将分发给 {{ selectedClasses.join('、') || '未选班级' }}</div>
          </div>
        </div>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, getCurrentInstance, defineEmits, defineExpose } from 'vue'
const context = getCurrentInstance()?.appContext.config.globalProperties
const emit = defineEmits(['formModelChange'])

const formVisible = ref(false); const formTitle = ref('新增测验'); const isAdd = ref(true)
const form = ref({ tasktitle: '' })
const formRef = ref(null)

const selectedClasses = ref([]); const classOptions = ref([])
const selectedGrade = ref(''); const selectedPoems = ref([])
const gradeOptions = ref("一年级,二年级,三年级,四年级,五年级,六年级".split(','))
const allPoems = ref([])
const filteredPoems = computed(() => selectedGrade.value ? allPoems.value.filter(p => p.grade === selectedGrade.value) : [])
const questionCount = ref(5)
const questions = ref([])
const generating = ref(false); const releasing = ref(false)
const tempTaskId = ref(0)

const parseOptions = (json) => { try { return JSON.parse(json) } catch(e) { return {} } }

// 加载班级（教师管辖班级）
const loadClassOptions = () => {
  context?.$http({ url: 'classinfo/list', method: 'get', params: { page:1, limit:999 } }).then(res => {
    classOptions.value = (res.data.data?.list || []).map(c => c.classname).filter(Boolean)
  }).catch(() => {})
}
// 加载古诗
const loadPoems = () => {
  context?.$http({ url: 'course/list', method: 'get', params: { page:1, limit:500 } }).then(res => {
    allPoems.value = (res.data.data?.list || []).map(c => ({ id: c.id, coursetitle: c.coursetitle, grade: c.grade }))
  }).catch(() => {})
}
const gradeChange = () => { selectedPoems.value = [] }

// AI 出题
const generateQuiz = () => {
  if (!form.value.tasktitle) { context?.$toolUtil.message('请输入测验标题', 'error'); return }
  generating.value = true; questions.value = []
  context?.$http({ url: 'quiztask/generate', method: 'post', data: {
    title: form.value.tasktitle, poetryIds: selectedPoems.value, questionCount: questionCount.value
  }}).then(res => {
    if (res.data.code === 0) {
      questions.value = res.data.data.questions || []
      tempTaskId.value = res.data.data.tempTaskId || 0
      context?.$toolUtil.message('AI 已生成 ' + questions.value.length + ' 道题，请审查', 'success')
    }
  }).finally(() => { generating.value = false })
}

// 删除题目
const removeQuestion = (idx) => { questions.value.splice(idx, 1) }

// 发布
const releaseQuiz = () => {
  releasing.value = true
  context?.$http({ url: 'quiztask/release', method: 'post', data: {
    title: form.value.tasktitle, tempTaskId: tempTaskId.value,
    classIds: selectedClasses.value, questions: questions.value
  }}).then(res => {
    if (res.data.code === 0) {
      context?.$toolUtil.message('已发布给 ' + res.data.data.studentCount + ' 名学生', 'success')
      formVisible.value = false; emit('formModelChange')
    }
  }).finally(() => { releasing.value = false })
}

const init = () => {
  form.value = { tasktitle: '' }; selectedClasses.value = []
  selectedGrade.value = ''; selectedPoems.value = []
  questionCount.value = 5; questions.value = []; tempTaskId.value = 0
  formTitle.value = '新增测验'; isAdd.value = true
  loadClassOptions(); loadPoems()
  formVisible.value = true
}

defineExpose({ init })
</script>

<style lang="scss" scoped>
:deep(.task_dialog .el-dialog) { border-radius: 28px; overflow:hidden; background: radial-gradient(circle at top right, rgba(255,211,146,.28), transparent 24%), linear-gradient(180deg, #fffdf8 0%, #ffffff 24%, #fffefb 100%); box-shadow: 0 28px 80px rgba(100,78,41,.18); }
:deep(.task_dialog .el-dialog__header) { padding: 24px 30px 0; }
:deep(.task_dialog .el-dialog__title) { color:#2f2a1f; font-size:28px; font-weight:700; }
:deep(.task_dialog .el-dialog__body) { padding: 10px 30px 24px; }
.dialog_intro { border:1px solid rgba(225,196,129,.45); border-radius:24px; padding:22px 24px; margin:8px 0 20px; background:linear-gradient(135deg, rgba(255,248,226,.95) 0%, rgba(245,252,247,.96) 100%); }
.intro_badge { display:inline-flex; padding:6px 14px; border-radius:999px; background:#fff; color:#7b5b17; font-size:12px; font-weight:700; }
.intro_title { margin-top:14px; color:#2d2417; font-size:24px; font-weight:700; }
.intro_desc { margin-top:8px; color:#72654a; font-size:14px; }
.formModel_form { border:0; padding:0; background:transparent; }
.form_section { border:1px solid rgba(231,220,194,.9); border-radius:24px; padding:24px 24px 12px; margin-bottom:18px; background:rgba(255,255,255,.88); }
.section_title { margin:0 0 18px; color:#3f3424; font-size:17px; font-weight:700; padding-left:14px; position:relative; }
.section_title::before { content:''; position:absolute; left:0; top:4px; width:4px; height:18px; border-radius:999px; background:linear-gradient(180deg,#f3b74b,#6abf73); }
.question_cards { display:flex; flex-direction:column; gap:14px; }
.question_card { border-radius:16px; border:1px solid #efe5cd; }
.card_header { display:flex; align-items:center; gap:10px; }
.card_idx { font-weight:700; color:#5b503f; }
.option_grid { display:grid; grid-template-columns:1fr 1fr; gap:8px; }
</style>
