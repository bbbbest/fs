import cn.zzu.rpc.RpcClient;
import cn.zzu.rpc.RpcRegistry;
import test.AnimalAction;

public class RpcClientTest {
    public static void main(String[] args) {
        RpcRegistry registry = new RpcRegistry("127.0.0.1", 8888);

        RpcClient client = RpcClient.create(registry);
        client.connect();
        AnimalAction action = client.referTo(AnimalAction.class);
//        action.sleep();
        action.learn();
        action.eat();
    }
}
