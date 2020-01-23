package pers.cy.speedkillsystem.result;

/**
 * rest api json输出
 * 规定返回值含义
 */
public class Result<T> {
    // 响应码，默认0是成功
    private int code;
    // 和响应码一一对应的响应信息
    private String msg;
    // 可以存一些附加对象，因为还不知道要存什么对象，所以用泛型来代替
    private T data;

    /**
     * 成功时候的调用
     * @param data
     * @param <T>
     * @return
     */
    public static <T> Result<T> success(T data) {
        return new Result<T>(data);
    }

    /**
     * 失败时候的调用
     * @param codeMsg  里面存放了错误代码以及对应的错误信息
     * @param <T>
     * @return
     */
    public static <T> Result<T> error(CodeMsg codeMsg) {
        return new Result<T>(codeMsg);

    }
    // 成功时的构造函数 成功才需要返回附加数据  private类型是因为不希望用户直接通过构造方法来创建对象，而是使用我们自己定义的方法去构造
    // 成功的话code和msg是固定的，只有返回的附加信息不同，所以只传入这个就可以
    private Result(T data) {
        this.code = 0;
        this.msg = "success";
        this.data = data;
    }
    // 失败时的构造函数  失败就不需要返回附加数据了，只需要写错误代码和对应的错误信息就可以了
    private Result(CodeMsg codeMsg) {
        if (codeMsg == null) {
            return;
        }
        this.code = codeMsg.getCode();
        this.msg = codeMsg.getMsg();
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }
}
