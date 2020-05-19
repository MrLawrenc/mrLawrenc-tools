# 这是一个个人工具集
## filter
- 这是一个filter工具类，使用者的业务方法需要实现Invoker接口
- 交给spring管理或者配置filter.basepkg属性，程序会自动扫描包下的所有Invoker实现类
- 之后用户若需要在Invoker执行业务前后进行操作，只需要实现对应的Filter接口即可（InboundFilter和OutboundFilter）
