app.controller('brandController',function($scope,$controller,brandService){
     $controller('baseController',{$scope:$scope});//继承 伪继承 数据互通
    	//查询品牌列表
    	$scope.findAll =function(){
    		brandService.findAll().success(
    				function (response) {
						$scope.list=response;
					}
    				);
    	}
    
        //分页
        $scope.findPage =function(page,size){
    		brandService.findPage(page,size).success(
    				function (response) {
    					$scope.list=response.rows;//显示当前页数据
    					$scope.paginationConf.totalItems=response.total;//更新总记录数
    				}
    				);
    	}
        
        //查询实体
    	$scope.findOne =function(id){
    		brandService.findOne(id).success(
    				function (response) {
						$scope.entity=response;
					}
    				);
    	}
        //添加
        $scope.save=function(){
        	var object=null;
        	if($scope.entity.id!=null){
        		object=brandService.update($scope.entity);
        	}else{
        		object=brandService.add($scope.entity);
        	}
        	object.success(
        			function(response){   
        				if(response.success){
        					$scope.reloadList();//刷新
        			}else{
        				alert(response.message);
        				}
        			}
        		);
        }
           
        
        
        //批量删除
        $scope.dele=function(){
        	//获取选中复选框
        	brandService.dele($scope.selectIds).success(
        		function(response){   
    				if(response.success){
    					$scope.reloadList();//刷新
    			}else{
    				alert(response.message);
    				}
    			}
        	);
        	
        }
        
        $scope.searchEntity={};//定义搜索对象
        //分页条件查询
        $scope.search=function(page,size){
        	
        	brandService.search(page,size,$scope.searchEntity).success(
    				function (response) {
    					$scope.list=response.rows;//显示当前页数据
    					$scope.paginationConf.totalItems=response.total;//更新总记录数
    				}
    				);
        }
        
 
    });