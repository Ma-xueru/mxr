import toolUtil from "@/utils/toolUtil";
const menu = {
  list() {
    if (toolUtil.storageGet("menus")) {
      return eval("(" + toolUtil.storageGet("menus") + ")");
    } else {
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
                  appFrontIcon: "cuIcon-pay",
                  buttons: ["新增", "查看", "修改", "删除"],
                  menu: "公告信息",
                  menuJump: "列表",
                  tableName: "news",
                },
              ],
              fontClass: "icon-common46",
              menu: "公告信息",
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
            {
              child: [
                {
                  appFrontIcon: "cuIcon-circle",
                  buttons: ["新增", "查看", "修改", "删除"],
                  menu: "答题试卷",
                  tableName: "exampaper",
                },
              ],
              fontClass: "icon-common27",
              menu: "国学答题库管理",
              unicode: "&#xee2c;",
            },
            {
              child: [
                {
                  appFrontIcon: "cuIcon-addressbook",
                  buttons: ["新增", "查看", "修改", "删除", "打印", "导出"],
                  menu: "问答题库管理",
                  menuJump: "列表",
                  tableName: "examquestion",
                },
              ],
              fontClass: "icon-common47",
              menu: "国学答题库管理",
              unicode: "&#xef63;",
            },
            {
              child: [
                {
                  appFrontIcon: "cuIcon-circle",
                  buttons: ["新增", "查看", "修改", "删除"],
                  menu: "古诗词测试列表",
                  menuJump: "12",
                  tableName: "exampaper",
                },
                {
                  appFrontIcon: "cuIcon-pic",
                  buttons: ["新增", "查看", "修改", "删除"],
                  menu: "测试记录",
                  tableName: "examrecord",
                },
                {
                  appFrontIcon: "cuIcon-pic",
                  buttons: ["新增", "查看", "修改", "删除"],
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
                  buttons: ["新增", "查看", "修改", "删除", "审核"],
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
                  appFrontIcon: "cuIcon-discover",
                  buttons: ["新增", "查看", "修改", "删除"],
                  menu: "师生绑定",
                  menuJump: "列表",
                  tableName: "mystudent",
                },
              ],
              fontClass: "icon-common38",
              menu: "师生绑定管理",
              unicode: "&#xeeb2;",
            },
            {
              child: [
                {
                  appFrontIcon: "cuIcon-paint",
                  buttons: ["新增", "查看", "修改", "删除", "查看评论"],
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
                  appFrontIcon: "cuIcon-pic",
                  buttons: ["查看", "成绩统计"],
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
                  appFrontIcon: "cuIcon-discover",
                  buttons: ["查看"],
                  menu: "师生绑定",
                  menuJump: "列表",
                  tableName: "mystudent",
                },
              ],
              fontClass: "icon-common38",
              menu: "师生绑定管理",
              unicode: "&#xeeb2;",
            },
            {
              child: [
                {
                  appFrontIcon: "cuIcon-paint",
                  buttons: ["新增", "查看", "修改", "删除", "查看评论"],
                  menu: "学习社区",
                  menuJump: "列表",
                  tableName: "forum",
                },
              ],
              fontClass: "icon-common23",
              menu: "学习社区管理",
              unicode: "&#xee05;",
            },
            {
              child: [
                {
                  appFrontIcon: "cuIcon-pay",
                  buttons: ["新增", "查看", "修改", "删除", "查看评论"],
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
                  appFrontIcon: "cuIcon-attentionfavor",
                  buttons: ["查看", "修改", "删除", "成绩统计", "新增"],
                  menu: "成绩信息",
                  menuJump: "列表",
                  tableName: "transcript",
                },
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
                  menu: "跟读记录",
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
        },
      ];
    }
  },
};
export default menu;
