import toolUtil from "@/utils/toolUtil";
const menu = {
  list() {
    if (toolUtil.storageGet("menus")) {
      var cached = toolUtil.storageGet("menus")
      if (cached === '[]' || cached === 'null' || cached.length < 10) { toolUtil.storageRemove("menus"); }
      else {
        cached = cached.replace(/跟读记录/g, "自主学习管理")
        toolUtil.storageSet("menus", cached)
        return eval("(" + cached + ")");
      }
    }
    {
      return [
        {
          backMenu: [
            {
              child: [
                {
                  appFrontIcon: "cuIcon-discover",
                  buttons: ["新增", "查看", "修改", "删除"],
                  menu: "轮播图",
                  menuJump: "列表",
                  tableName: "config",
                },
              ],
              fontClass: "icon-common18",
              menu: "轮播图管理",
              unicode: "&#xedff;",
            },
            {
              child: [
                {
                  appFrontIcon: "cuIcon-keyboard",
                  buttons: ["新增", "查看", "修改", "删除"],
                  menu: "管理员",
                  menuJump: "列表",
                  tableName: "admin",
                },
                {
                  appFrontIcon: "cuIcon-full",
                  buttons: ["新增", "查看", "修改", "删除", "首页总数", "分配"],
                  menu: "用户",
                  menuJump: "列表",
                  tableName: "student",
                },
                {
                  appFrontIcon: "cuIcon-qrcode",
                  buttons: ["新增", "查看", "修改", "删除", "首页总数"],
                  menu: "教师",
                  menuJump: "列表",
                  tableName: "teacher",
                },
              ],
              fontClass: "icon-common50",
              menu: "用户信息管理",
              unicode: "&#xef96;",
            },
            {
              child: [
                {
                  appFrontIcon: "cuIcon-pay",
                  buttons: ["新增", "查看", "修改", "删除", "查看评论"],
                  menu: "古诗词管理信息",
                  menuJump: "列表",
                  tableName: "course",
                },
              ],
              fontClass: "icon-common49",
              menu: "古诗词管理",
              unicode: "&#xef3d;",
            },
            {
              child: [
                {
                  appFrontIcon: "cuIcon-group",
                  buttons: ["新增", "查看", "修改", "删除"],
                  menu: "班级管理",
                  menuJump: "列表",
                  tableName: "classinfo",
                },
              ],
              fontClass: "icon-common50",
              menu: "班级管理",
              unicode: "&#xef96;",
            },
          ],
          frontMenu: [
            {
              child: [
                {
                  appFrontIcon: "cuIcon-time",
                  buttons: ["查看"],
                  menu: "教师",
                  menuJump: "列表",
                  tableName: "teacher",
                },
              ],
              menu: "教师管理",
            },
            {
              child: [
                {
                  appFrontIcon: "cuIcon-skin",
                  buttons: ["新增", "查看", "查看评论"],
                  menu: "学习社区",
                  menuJump: "列表",
                  tableName: "forum",
                },
              ],
              menu: "学习社区管理",
            },
          ],
          hasBackLogin: "是",
          hasBackRegister: "否",
          hasFrontLogin: "否",
          hasFrontRegister: "否",
          roleName: "管理员",
          tableName: "admin",
        },
        {
          backMenu: [
            {
              child: [
                {
                  appFrontIcon: "cuIcon-pic",
                  buttons: ["查看"],
                  menu: "测试记录",
                  tableName: "examrecord",
                },
                {
                  appFrontIcon: "cuIcon-pic",
                  buttons: ["查看"],
                  menu: "错题本",
                  menuJump: "22",
                  tableName: "examrecord",
                },
              ],
              fontClass: "icon-common18",
              menu: "题库管理",
              unicode: "&#xedff;",
            },
            {
              child: [
                {
                  appFrontIcon: "cuIcon-send",
                  buttons: ["新增", "查看", "修改", "删除"],
                  menu: "意见反馈",
                  menuJump: "列表",
                  tableName: "feedback",
                },
              ],
              fontClass: "icon-common28",
              menu: "意见反馈管理",
              unicode: "&#xee2d;",
            },
            {
              child: [
                {
                  appFrontIcon: "cuIcon-paint",
                  buttons: ["查看", "修改", "删除", "查看评论"],
                  menu: "学习社区",
                  menuJump: "列表",
                  tableName: "forum",
                },
              ],
              fontClass: "icon-common23",
              menu: "学习社区管理",
              unicode: "&#xee05;",
            },
          ],
          frontMenu: [
            {
              child: [
                {
                  appFrontIcon: "cuIcon-time",
                  buttons: ["查看"],
                  menu: "教师",
                  menuJump: "列表",
                  tableName: "teacher",
                },
              ],
              menu: "教师管理",
            },
            {
              child: [
                {
                  appFrontIcon: "cuIcon-skin",
                  buttons: ["新增", "查看", "查看评论"],
                  menu: "学习社区",
                  menuJump: "列表",
                  tableName: "forum",
                },
              ],
              menu: "学习社区管理",
            },
          ],
          hasBackLogin: "否",
          hasBackRegister: "否",
          hasFrontLogin: "是",
          hasFrontRegister: "是",
          roleName: "用户",
          tableName: "student",
        },
        {
          backMenu: [
            {
              child: [
                {
                  appFrontIcon: "cuIcon-home",
                  buttons: ["查看"],
                  menu: "🏠 教师工作台",
                  menuJump: "dashboard",
                  tableName: "teacherDashboard",
                },
              ],
              fontClass: "icon-common80",
              menu: "🏠 教师工作台",
              unicode: "&#xe6b4;",
            },
            {
              child: [
                {
                  appFrontIcon: "cuIcon-group",
                  buttons: ["查看", "修改"],
                  menu: "班级管理",
                  menuJump: "列表",
                  tableName: "classinfo",
                },
              ],
              fontClass: "icon-common50",
              menu: "班级管理",
              unicode: "&#xef96;",
            },
            {
              child: [
                {
                  appFrontIcon: "cuIcon-full",
                  buttons: ["新增", "查看", "修改", "删除"],
                  menu: "学生管理",
                  menuJump: "列表",
                  tableName: "student",
                },
              ],
              fontClass: "icon-common38",
              menu: "学生管理",
              unicode: "&#xeeb2;",
            },
            {
              child: [
                {
                  appFrontIcon: "cuIcon-pay",
                  buttons: ["查看"],
                  menu: "古诗文库",
                  menuJump: "列表",
                  tableName: "course",
                },
              ],
              fontClass: "icon-common49",
              menu: "古诗文库管理",
              unicode: "&#xef3d;",
            },
{
              child: [
                {
                  appFrontIcon: "cuIcon-book",
                  buttons: ["新增", "查看", "修改", "删除"],
                  menu: "背诵任务",
                  menuJump: "列表",
                  tableName: "recitationtask",
                },
                {
                  appFrontIcon: "cuIcon-group",
                  buttons: ["查看", "删除"],
                  menu: "自主学习管理",
                  menuJump: "列表",
                  tableName: "followreadrecord",
                },
                {
                  appFrontIcon: "cuIcon-edit",
                  buttons: ["新增", "查看", "修改", "删除"],
                  menu: "测验管理",
                  menuJump: "列表",
                  tableName: "quiztask",
                },
              ],
              fontClass: "icon-common31",
              menu: "学习任务管理",
              unicode: "&#xee48;",
            },
          ],
          frontMenu: [
            {
              child: [
                {
                  appFrontIcon: "cuIcon-home",
                  buttons: ["查看"],
                  menu: "教师工作台",
                  menuJump: "dashboard",
                  tableName: "teacherDashboard",
                },
              ],
              menu: "快捷入口",
            },
            {
              child: [
                {
                  appFrontIcon: "cuIcon-time",
                  buttons: ["查看"],
                  menu: "教师",
                  menuJump: "列表",
                  tableName: "teacher",
                },
              ],
              menu: "教师管理",
            },
            {
              child: [
                {
                  appFrontIcon: "cuIcon-skin",
                  buttons: ["新增", "查看", "查看评论"],
                  menu: "学习社区",
                  menuJump: "列表",
                  tableName: "forum",
                },
              ],
              menu: "学习社区管理",
            },
          ],
          hasBackLogin: "是",
          hasBackRegister: "是",
          hasFrontLogin: "否",
          hasFrontRegister: "否",
          roleName: "教师",
          tableName: "teacher",
          isTeacher: true,
        },
      ];
    }
  },
};
export default menu;
