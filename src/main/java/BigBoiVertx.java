import DB.DbI;
import DB.Model.Transaction;
import DB.PostGreSQLDb;
import DB.Model.Transaction;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.LinkedList;

public class BigBoiVertx extends AbstractVerticle {

    private static String ROOT = "/rest/";
    DbI dbReference = new PostGreSQLDb();

    @Override
    public void start() {
        Router router = Router.router(vertx);
        router.route("/").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.putHeader("content-type","text/html").end("<h1>yoyo</h1>");
        });
        router.route(HttpMethod.POST, "/*").handler(BodyHandler.create());
        router.get(ROOT+"tjo").handler(this::sayTjo);
        router.get(ROOT+"getAllTransactions").handler(this::getAllTransactions);
        router.post(ROOT+"makeTransaction").handler(this::makeTransactions);
        router.post(ROOT+"login").handler(this::authenticateUser);
        router.get(ROOT+"getUsers").handler(this::getUsers);
        router.get(ROOT+"getNrOfTransactions").handler(this::getNrOfTransactions);
        vertx.createHttpServer().requestHandler(router::accept).listen(7089);
        System.out.println("WE HAWT");
        dbReference.createConnection();

    }
    private void sayTjo(RoutingContext rc){
        rc.response().setStatusCode(201).putHeader("content-type","text/html").end("tjo");
    }

    private void authenticateUser(RoutingContext rc){
        String string = rc.getBodyAsString();
        System.out.println("FORM DATA authenticate: "+string);
        String[] strings = string.split("&");
        String username = strings[0].split("=")[1];
        String password = strings[1].split("=")[1];
        System.out.println(username + " "+ password);
        if(dbReference.authenticateUser(username,password)){
            rc.response().setStatusCode(200).putHeader("content-type", "text/html").end("Successful login!");
        }else
            rc.response().setStatusCode(401).putHeader("content-type", "text/html").end("Unsuccessful login!");
    }

    private void getAllTransactions(RoutingContext rc){
        String username = rc.request().getParam("name");
        System.out.println("FORM DATA TRANSACTION: "+username);
        LinkedList<Transaction> list = (LinkedList) dbReference.retrieveAllTransactions(username);
        if(list!=null){
            rc.response().setStatusCode(200).putHeader("content-type","application/json; charset=utf-8")
                    .end(Json.encodePrettily(list));
        }
        else
            rc.response().setStatusCode(204).putHeader("content-type","text/html").end("No data BOI");

    }

    private void getNrOfTransactions(RoutingContext rc){
        String username = rc.request().getParam("name");
        int nrOfTransaction = Integer.parseInt(rc.request().getParam("nrOfTransactions"));
        LinkedList<Transaction> list = (LinkedList) dbReference.retrieveNrOfTransactions(username,nrOfTransaction);
        if(list!=null){
            rc.response().setStatusCode(200).putHeader("content-type","application/json; charset=utf-8")
                    .end(Json.encodePrettily(list));
        }
        else
            rc.response().setStatusCode(204).putHeader("content-type","text/html").end("No data BOI");
    }

    private void makeTransactions(RoutingContext rc){
        String string = rc.getBodyAsString();
        System.out.println("FORM DATA MAKE TRANSACTION: "+string);
        String[] strings = string.split("&");
        String usernameTo = strings[0].split("=")[1];
        String usernameFrom = strings[1].split("=")[1];
        String amount = strings[2].split("=")[1];

        if(dbReference.makeTransaction(usernameTo,usernameFrom,Double.parseDouble(amount))){
            rc.response().setStatusCode(200).putHeader("content-type","text/html").end("GOOD REQ");
        }else
            rc.response().setStatusCode(400).putHeader("content-type","text/html").end("BAD REQ");

    }

    private void getUsers(RoutingContext rc){
        String string = rc.getBodyAsString();
        System.out.println("FORM DATA GET USERS: "+string);
        LinkedList<String> list = (LinkedList) dbReference.retrieveAllUsernames();
        if(list != null){
            rc.response().setStatusCode(200).putHeader("content-type","application/json; charset=utf-8")
                    .end(Json.encodePrettily(list));
        }else
            rc.response().setStatusCode(204).putHeader("content-type","text/html").end("No data BOI");

    }

}
