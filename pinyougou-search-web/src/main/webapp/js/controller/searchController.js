var app = new Vue({
    el:"#app",
    data:{
        //搜索条件对象
        searchMap:{"keywords":"","brand":"","category":"", "spec":{}, "price":"","pageNo":1, "pageSize":10, "sortField":"","sortOrder":""},
        //返回结果
        resultMap: {"itemList":[]},
        //页号数组
        pageNoList:[],
        //分页导航栏前面3个点
        frontDot:false,
        //分页导航栏后面3个点
        backDot:false
    },
    methods:{
        //排序查询
        sortSearch: function(sortField, sortOrder){
            this.searchMap.sortField = sortField;
            this.searchMap.sortOrder = sortOrder;
            this.searchMap.pageNo = 1;
            this.search();
        },
        //根据页号查询
        queryByPageNo:function (pageNo) {
          if( 0 < pageNo && pageNo <= this.resultMap.totalPages){
              this.searchMap.pageNo = pageNo;
              this.search();
          }
        },
        //删除过滤条件
        removeSearchItem:function(key){
            if (key == "brand" || key == "category" || key == "price") {
                this.searchMap[key] = "";
            }else{
                this.$set(this.searchMap.spec,key,null);
                //并且删除属性
                delete this.searchMap.spec[key];
            }
            //改变了过滤查询条件，需要重置页号为1
            this.searchMap.pageNo = 1;
            this.search();
        },
        //添加过滤条件
        addSearchItem:function(key, value){
            //如果是品牌或者是分类，则直接加入searchMap
            if (key == "brand" || key == "category" || key == "price") {
                this.searchMap[key] = value;
            }else{
                //如果是规格，则加入到spec属性，使用$set方法出发视图更新
                //参数1，2，3:要更新的对象，对象中属性名，属性值
                this.$set(this.searchMap.spec,key,value);
            }
            //改变了过滤查询条件，需要重置页号为1
            this.searchMap.pageNo = 1;
            this.search();
        },
        //构建分页导航条
        buildPagination:function () {
            this.pageNoList = [];
            //1.总页数小于等于要显示的页号数的时候，则全部页号显示
            //起始页号
            var startPageNo = 1;
            //结束页号
            var endPageNo = this.resultMap.totalPages;
            //要在页面中显示的总页数，默认5
            var totalShowPageNo = 5;

            // 2、总页数大于要显示的页号数的时候：
            if(this.resultMap.totalPages > totalShowPageNo){
                //当前页左右间隔
                var interval = Math.floor(totalShowPageNo/2);

                //起始页号：当前页-（要显示的页号/2）
                startPageNo = this.searchMap.pageNo - interval;
                //结束页号：当前页 + （要显示的页号/2）
                endPageNo = this.searchMap.pageNo + interval;
                if(startPageNo <=0){
                    //如果起始页小于等于0的时候，默认为1；结束页号等于要显示的页号数
                    startPageNo = 1;
                    endPageNo = totalShowPageNo;
                }if(endPageNo > this.resultMap.totalPages) {
                    // - 如果结束页号数大于总页数则置为总页数，起始页号为总页数-要显示的页号数+1
                    endPageNo = this.resultMap.totalPages;
                    startPageNo = endPageNo - totalShowPageNo +1;
                }
            }
            this.frontDot = false;
            this.backDot = false;

            //前面3个点
            if (startPageNo > 1) {
                this.frontDot = true;
            }
            //后面3个点
            if (endPageNo < this.resultMap.totalPages) {
                this.backDot = true;
            }

            for (let i = startPageNo; i < endPageNo; i++) {
                this.pageNoList.push(i);
            }
        },
        //搜索
        search:function () {
            axios.post("itemSearch/search.do", this.searchMap).then(function (response) {
                app.resultMap = response.data;
                app.buildPagination();
            });
        },
        //根据参数名字获取参数
        getParameterByName: function (name) {
            return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.href) || [, ""])[1].replace(/\+/g, '%20'))
        }
    },
    created(){
        //获取浏览器地址栏中的搜索关键字
        this.searchMap.keywords = this.getParameterByName("keywords");
        //默认查询所有
        this.search();
    }
});