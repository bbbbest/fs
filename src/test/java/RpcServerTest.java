import cn.zzu.rpc.RpcRegistry;
import cn.zzu.rpc.RpcServer;
import test.AnimalAction;
import test.Man;

public class RpcServerTest {
    public static void main(String[] args) throws InterruptedException {
        AnimalAction action = new Man();
        RpcRegistry registry = new RpcRegistry("127.0.0.1", 8888);
        RpcServer server = RpcServer.create(registry);
        server.export(AnimalAction.class, action);
        server.start();
    }
}
