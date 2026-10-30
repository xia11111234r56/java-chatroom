//InfoType 枚举类定义了整个聊天系统中客户端和服务器之间传输数据时使用的消息类型（或叫“状态码”）。
//它的核心作用是标识当前 TransferInfo 消息对象所代表的功能或用途，比如是登录、聊天、通知等
package jiwangkeshe;
public enum InfoType 
{
    LOGIN(1, "登录"),//123456为状态码
    NOTICE(2, "系统消息"),
    CHAT(3, "聊天消息"),
    ULIST(4, "在线用户列表"),
    EXIT(5, "退出"),
    REGISTER(6, "注册"),;
    private Integer status;//每个枚举对象对应一个 status（状态码）
    private String desc;//和一个 desc（描述字符串）
    // 构造方法（注意：status参数类型应与字段类型匹配）
    private InfoType(Integer status, String desc) 
    {  // 修改参数类型为Integer
        this.status = status;
        this.desc = desc;
    }
    //获取当前枚举值对应的 status 状态码
    public Integer getStatus() {
        return status;
    }
    //修改状态码
    public void setStatus(Integer status) {
        this.status = status;
    }
    public String getDesc() {
        return desc;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }
}