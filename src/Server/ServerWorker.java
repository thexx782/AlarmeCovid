package Server;

import Demultiplexer.TaggedConnection;
import User.Localizacao;
import User.User;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServerWorker {
    final static int WORKERS_PER_CONNECTION = 10;

    public static void serverWorkerStart(ServerSocket ss) throws IOException {

        UserList users = new UserList();
        List<Localizacao> buffer = new ArrayList<>();

        while (true) {
            Socket s = ss.accept();
            TaggedConnection c = new TaggedConnection(s);

            Runnable worker = () -> {
                try (c) {
                    for (; ; ) {
                        TaggedConnection.DataFrame frame = c.receiveUser();
                        switch (frame.tag) {
                            case 0:
                                Localizacao loc = new Localizacao(frame.x, frame.y);
                                User u = new User(frame.user, frame.pass, loc);
                                users.addUser(u);
                                System.out.println("Users: ");
                                users.printUsers();
                                System.out.println();
                                break;
                            case 1:
                                if (users.autenticarUser(frame.user, frame.pass) && !users.getUser(frame.user).isInfetado()) {
                                    c.sendUser(1, "User", "Pass", 0, 0, true, false, 0, 0);
                                }
                                else
                                    c.sendUser(1, "User", "Pass", 0, 0, false, false, 0, 0);
                                break;
                            case 2:
                                System.out.println("----------------Localizar----------------");
                                System.out.println("Current ThreadID: " + Thread.currentThread().getId());
                                System.out.println(frame.user + " vai para : " + "(" + frame.x + "," + frame.y + ")");
                                int xAtual = users.getUser(frame.user).getLocalizacaoAtual().getX();
                                int yAtual = users.getUser(frame.user).getLocalizacaoAtual().getY();
                                users.alterarLoc(frame.user, frame.x, frame.y);
                                List<Localizacao> aRemover = new ArrayList<>();
                                for (Localizacao l : buffer) {
                                    System.out.println("User.Localizacao do buffer[x]: " + l);
                                    if (users.numPessoas(l.getX(), l.getY()) == 0) {
                                        for (Map.Entry<String, User> uz : users.getMap().entrySet()) {
                                            String xy = "(" + xAtual + "," + yAtual + ")";
                                            System.out.println("Verificar se o " + uz.getValue().getUsername() + " quer vir para " + xy);

                                            if (!uz.getValue().getUsername().equals(frame.user) && uz.getValue().getLocalizacaoDest() != null &&
                                                    uz.getValue().getLocalizacaoDest().getX() == xAtual &&
                                                    uz.getValue().getLocalizacaoDest().getY() == yAtual) {
                                                uz.getValue().setLocalizacaoDest(null);
                                                aRemover.add(new Localizacao(xAtual, yAtual));
                                                System.out.println(uz.getValue().getUsername() + " quer vir para" + xy + ", logo " + xy + " é elimidado do buffer");
                                            } else
                                                System.out.println("Não quer vir!");

                                        }
                                    }
                                }
                                buffer.removeAll(aRemover);
                                if (!buffer.isEmpty())
                                    System.out.println("Buffer de Localizações desejadas:");
                                for (Localizacao l : buffer)
                                    System.out.println(l);
                                System.out.println("-----------------------------------------\n");
                                break;
                            case 3:
                                int y = users.numPessoas(frame.x, frame.y);
                                c.sendUser(3, "", "", y, 0, false, false, 0, 0);
                                break;
                            case 4:
                                loc = new Localizacao(frame.x, frame.y);

                                if (users.numPessoas(frame.x, frame.y) == 0)
                                    c.sendUser(4, "", "", loc.getX(), loc.getY(), true, false, 0, 0);
                                else {
                                    System.out.println("Current ThreadID: " + Thread.currentThread().getId());
                                    buffer.add(loc);
                                    users.getUser(frame.user).setLocalizacaoDest(new Localizacao(frame.x, frame.y));
                                    while (users.getUser(frame.user).getLocalizacaoDest() != null) {

                                    }
                                    c.sendUser(4, "", "", loc.getX(),
                                            loc.getY(), true, false, 0, 0);
                                    users.getUser(frame.user).setLocalizacaoDest(null);
                                }
                                break;
                            case 5:
                                users.getUser(frame.user).setInfetado(true);
                                users.printUsers();
                                break;
                            case 6:
                                for(String a : users.sitiosInfetados(frame.user)){
                                    System.out.println(a);
                                    c.sendUser(6,a,"",0,0,true,true,0,0);
                                }
                                break;
                        }
                    }
                } catch (Exception ignored) {
                }
            };

            for (int i = 0; i < WORKERS_PER_CONNECTION; ++i)
                new Thread(worker).start();
        }
    }
}
