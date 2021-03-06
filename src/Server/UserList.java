package Server;

import User.User;
import User.Localizacao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserList {
    private Map<String, User> users;

    public UserList() {
        this.users = new HashMap<>();
        Localizacao l1 = new Localizacao(1,1);
        Localizacao l2 = new Localizacao(2,2);
        Localizacao l3 = new Localizacao(3,3);

        User ana = new User("Ana","ana",null,new ArrayList<>(),
                false,false);

        User pedro = new User("Pedro","pedro",null,new ArrayList<>(),
                false,false);

        User goncalo = new User("Goncalo","goncalo",null,new ArrayList<>(),
                false,false);

        ana.addLocalizacao(l1);
        pedro.addLocalizacao(l2);
        goncalo.addLocalizacao(l3);

        users.put(ana.getUsername(),ana);
        users.put(pedro.getUsername(),pedro);
        users.put(goncalo.getUsername(), goncalo);
    }

    public void addUser(User u){
        users.put(u.getUsername(),u);
    }

    public void printUsers(){
        for(Map.Entry<String,User> e : this.users.entrySet()){
            System.out.println(e);
        }
    }

    public boolean autenticarUser(String user,String pass){
        boolean r = false;
        for(Map.Entry<String,User> e : this.users.entrySet()){
            if(e.getValue().getUsername().equals(user) && e.getValue().getPassword().equals(pass)){
                r = true;
                e.getValue().setLoged(true);
            }
        }
        return r;
    }

    public void alterarLoc(String user,int x,int y){
        for(Map.Entry<String,User> e : this.users.entrySet()){
            if(e.getValue().getUsername().equals(user)){
                Localizacao loc = new Localizacao(x,y);
                e.getValue().addLocalizacao(loc);
            }
        }
    }

    public int numPessoas(int x,int y){
        int count = 0;
        for(Map.Entry<String,User> e : this.users.entrySet()){
            if(e.getValue().getLocalizacaoAtual().getX() == x && e.getValue().getLocalizacaoAtual().getY() == y)
                count ++;
        }
        return count;
    }

    public User getUser(String user){
        return this.users.get(user);
    }

    public Map<String,User> getMap(){
        return this.users;
    }

    public List<String> sitiosInfetados(String user){
        List<String> listaDeUsers  = new ArrayList<>();
        User a = users.get(user);
        for (Map.Entry<String,User> b : users.entrySet()){
            System.out.println(" Usuário a ser Analisado:  " + b);
            for(Localizacao loc : a.getLocalizacoes()) {
                if (b.getValue().getLocalizacoes().contains(loc) && !b.getValue().getUsername().equals(user))
                    System.out.println("Localização contaminada: " + b.getValue().getLocalizacoes());
                    listaDeUsers.add(b.getValue().getUsername());
                    System.out.println("Usuário adicionado: " + b.getValue().getUsername());
            }
        }
        return listaDeUsers;
    }
}
