var app = new Vue({
    el:"#app",
    data:{
        //广告数据
        contentData:{1:[]},
        //关键字
        keywords:""
    },
    methods: {
        //搜索方法
        search: function () {
            location.href = "http://search.pinyougou.com?keywords=" + this.keywords;
        },
        //根据内容分类id查询内容列表
        findContentListByCategoryId: function (categoryId) {
            axios.get("../content/findContentListByCategoryId.do?categoryId=" + categoryId).then(function (response) {
                app.contentData[categoryId] = response.data;
            });

        }
    },
    created(){
        this.findContentListByCategoryId(1);
    }
});