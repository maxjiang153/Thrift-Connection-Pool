<h1>Thrift连接池实现</h1>

目标：<br/>
  1、支持池化管理thrift客户端连接<br/>
  2、支持thrift服务器的负载均衡<br/>
  3、支持thrift服务器列表的动态管理<br/>

<h1>示例</h1>
<strong>https://github.com/wmz7year/Thrift-Connection-Pool/blob/master/src/test/java/com/wmz7year/thrift/pool/config/TestPool.java</strong>

目前完成的进度：<br/>
	
	支持根据现有服务器列表自动维护获取连接 

<h1>接下来需要完善内容：</h1>
 1、需要完成三个服务器监听线程<br/>
 2、服务器列表变化时连接分区响应的也变化<br/>
 3、代码格式整理<br/>
 4、补充单元测试<br/>
 5、补充文档<br/>
 6、补充性能测试<br/>


