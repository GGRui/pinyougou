//创建vue实例
var app = new Vue({
    el:"#app",
    //数据
    data:{
        //品牌列表
        entityList:[],
        //总记录数
        total:0,
        //页数
        pageSize:10,
        //当前页
        pageNum:1,
        //实体
        entity:{},
        //选中的ids
        ids:[],
        //查询条件对象
        searchEntity:{},
        //复选框默认值
        checked:false
    },
    methods:{
        //复选框全选和取消
        changeAllChecked:function () {
            var iid=[];
            for (var i = 0; i < app.entityList.length; i++) {
                var obj = app.entityList[i];
                console.log(obj.id);
                iid.push(obj.id)
            }
            if (this.checked){
                this.ids=iid
            }else {
                this.ids=[];
            }
        },
        //批量删除
        deleteList:function () {
            if (this.ids.length == 0){
                alert("请选择要删除的记录");
                return;
            }
            //确认是否要删除
            if(confirm("确定要删除选中的记录吗？")){
                axios.get("../brand/delete.do?ids=" + this.ids).then(function (response) {
                    if(response.data.success){
                        app.searchList(1);
                        //清空选择的id
                        app.ids = [];
                    }else{
                        alert(response.data.message);
                    }
                });
            }
        },
        //根据id查询
        findOne: function (id) {
            axios.get("../brand/findOne/" + id + ".do").then(function (response) {
                app.entity = response.data;
            });
        },
        //保存数据
        save:function(){
            axios.post("../brand/add.do",this.entity).then(function (response) {
                if(response.data.success){
                    //保存成功
                    app.searchList(1);
                }else {
                    //保存失败
                    alert(response.data.message);
                }
            });
        },
        //分页查询
        searchList:function (curPage) {
            this.pageNum=curPage;
            /*axios.get("../brand/findPage.do?pageNum="+this.pageNum+"&pageSize="+this.pageSize).then(function (response) {
                app.entityList=response.data.list;
                app.total=response.data.total;
            });*/

            axios.post("../brand/search.do?pageNum="+this.pageNum+"&pageSize="+this.pageSize,this.searchEntity).then(function (response) {

                //记录列表
                app.entityList=response.data.list;
                //符合本次查询的总记录数
                app.total=response.data.total;
            });
            this.ids=[];
        }
    },
    watch: {
        ids: function() {

            if (this.ids.length == this.entityList.length && this.entityList.length > 0) {
                this.checked = true
            } else {
                this.checked = false
            }
        }
    },
    // 传统方式写create:function(){}
    created(){//需要打开设置 languages&frameworks--javaScript  把版本号改成ECMAScript 6
        /*axios.get("../brand/findAll.do").then(function (response) {
            app.entityList = response.data;
        });*/
        this.searchList(this.pageNum);
    }
});