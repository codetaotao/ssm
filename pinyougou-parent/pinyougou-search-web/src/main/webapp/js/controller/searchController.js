app.controller('searchController',function($scope,searchService){ 
	//定义搜索对象的结构 category:商品分类
	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{}};
	
	//搜索
	$scope.search=function(){
		searchService.search($scope.searchMap).success(
				function(response){
					$scope.resultMap=response;//搜索返回的结果
				}
		 );
	 }
	
	//添加搜索项,改变searchMap的值
	$scope.addSerarchItem=function(key,value){
		if(key=="category"||key=="brand"){
			//如果用户点击的是品牌或者分类
			$scope.searchMap[key]=value;
			
		}else{
			//用户点的是规格
			$scope.searchMap.spec[key]=value;
		}
		$scope.search();//查询
	}
	
	//撤销搜索项
	$scope.removeSerarchItem=function(key){
		if(key=="category"||key=="brand"){
			//如果用户点击的是品牌或者分类
			$scope.searchMap[key]="";	
		}else{
			//用户点的是规格
			delete $scope.searchMap.spec[key];
		}
		$scope.search();//查询
	}
});