app.controller('baseController',function($scope){
	//分页控件配置
    $scope.paginationConf = { 
 		   currentPage: 1, //当前页的页码
 		   totalItems: 10, //总共有多少条
 		   itemsPerPage: 10, //每页显示多少条
 		   perPageOptions: [10, 20, 30, 40, 50], //每页展示的条数选项
 		   onChange: function(){ //分页选项,当上面四个值发生变化的时候会默认调用
 			  $scope.reloadList();
 		   }
 		   };
    
    //刷新列表
    $scope.reloadList=function(){
    	$scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
    }
    
    $scope.selectIds=[];//用户勾选的id集合
    $scope.updateSelection=function($event,id){
    	if($event.target.checked){
    		$scope.selectIds.push(id);//push向集合添加元素
    	}else{
    		var index=$scope.selectIds.indexOf(id);//查找值的位置
    		$scope.selectIds.splice(index,1);//参数一:移除的位置,参数二:移除的个数
    	}
    }
    
    $scope.jsonToString=function(jsonString,key){
    	var json = JSON.parse(jsonString);
    	var value="";
    	
    	
    	for (var i = 0; i < json.length; i++) {
    		if(i>0){
    			value+=",";
    		}
			value+=json[i][key]
		}
    	return value
    }
    
  //从集合中按照 key 查询对象
    $scope.searchObjectByKey=function(list,key,keyValue){
    	for(var i=0;i<list.length;i++){
    		if(list[i][key]==keyValue){
    			return list[i];
    		} 
    	} 
    	return null;
    }
    
    
    
    
})