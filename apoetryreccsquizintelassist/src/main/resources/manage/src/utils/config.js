const config = {
    get() {
        return {
            url : process.env.VUE_APP_BASE_API_URL + process.env.VUE_APP_BASE_API + '/',
            name: process.env.VUE_APP_BASE_API,
            // 退出到首页链接
            indexUrl: 'http://localhost:8080/apoetryreccsquizintelassist/client/h5/index.html'
        }
    },
    getProjectName(){
        return {
            projectName: "乡村儿童的古诗文背诵与国学问答智能助手后台"
        } 
    }
}
export default config
