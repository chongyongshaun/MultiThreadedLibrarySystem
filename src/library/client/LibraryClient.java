package library.client;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

import library.models.*;

public class LibraryClient {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 5000;

        try (Socket socket = new Socket(host, port);
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                Scanner sc = new Scanner(System.in)) {

            System.out.println("Connected to server at " + host + ":" + port);

            boolean running = true;
            while (running) {
                System.out.println("\nChoose an action:\n1) REGISTER\n2) LOGIN\n3) EXIT");
                String choice = sc.nextLine().trim();
                switch (choice) {
                    case "1": // register
                        System.out.print("Name: ");
                        String name = sc.nextLine();
                        System.out.print("Student ID: ");
                        String sid = sc.nextLine();
                        System.out.print("Email: ");
                        String email = sc.nextLine();
                        System.out.print("Password: ");
                        String pass = sc.nextLine();
                        System.out.print("Department: ");
                        String dept = sc.nextLine();
                        System.out.print("Role (STUDENT or LIBRARIAN): ");
                        String roleStr = sc.nextLine();
                        UserRole role = UserRole.valueOf(roleStr.toUpperCase());
                        User user = new User(name, sid, email, pass, dept, role);
                        oos.writeObject(new Request(RequestAction.REGISTER, user));
                        oos.flush();
                        Response regRes = (Response) ois.readObject();
                        System.out.println(regRes.getMessage());
                        break;

                    case "2": // login
                        System.out.print("Email: ");
                        String le = sc.nextLine();
                        System.out.print("Password: ");
                        String lp = sc.nextLine();
                        oos.writeObject(new Request(RequestAction.LOGIN, new LoginData(le, lp)));
                        oos.flush();
                        Response res = (Response) ois.readObject();
                        System.out.println(res.getMessage());
                        if (res.isSuccess()) {
                            // after login show authenticated menu
                            boolean loggedIn = true;
                            while (loggedIn) {
                                System.out.println(
                                        "\nAuthenticated menu:\n1) CREATE_RECORD\n2) GET_ALL_RECORDS\n3) ASSIGN_RECORD (librarian only)\n4) GET_ASSIGNED_RECORDS (librarian only)\n5) UPDATE_PASSWORD\n6) GET_ALL_USERS\n7) LOGOUT");
                                String a = sc.nextLine().trim();
                                switch (a) {
                                    case "1":
                                        System.out.print("RecordType (NEW_BOOK_ENTRY or BORROW_REQUEST): ");
                                        String rtStr = sc.nextLine();
                                        oos.writeObject(
                                                new Request(RequestAction.CREATE_RECORD, RecordType.valueOf(rtStr)));
                                        oos.flush();
                                        System.out.println(((Response) ois.readObject()).getMessage());
                                        break;
                                    case "2":
                                        oos.writeObject(new Request(RequestAction.GET_ALL_RECORDS, null));
                                        oos.flush();
                                        Response allRec = (Response) ois.readObject();
                                        System.out.println(allRec.getMessage());
                                        Object data = allRec.getData();
                                        if (data instanceof List) {
                                            List<?> list = (List<?>) data;
                                            list.forEach(System.out::println);
                                        }
                                        break;
                                    case "3":
                                        System.out.print("Record ID to assign: ");
                                        String rid = sc.nextLine();
                                        oos.writeObject(new Request(RequestAction.ASSIGN_RECORD, rid));
                                        oos.flush();
                                        System.out.println(((Response) ois.readObject()).getMessage());
                                        break;
                                    case "4":
                                        oos.writeObject(new Request(RequestAction.GET_ASSIGNED_RECORDS, null));
                                        oos.flush();
                                        Response asg = (Response) ois.readObject();
                                        System.out.println(asg.getMessage());
                                        if (asg.getData() instanceof List)
                                            ((List<?>) asg.getData()).forEach(System.out::println);
                                        break;
                                    case "5":
                                        System.out.print("New password: ");
                                        String np = sc.nextLine();
                                        oos.writeObject(new Request(RequestAction.UPDATE_PASSWORD, np));
                                        oos.flush();
                                        System.out.println(((Response) ois.readObject()).getMessage());
                                        break;
                                    case "6":
                                        oos.writeObject(new Request(RequestAction.GET_ALL_USERS, null));
                                        oos.flush();
                                        Response users = (Response) ois.readObject();
                                        System.out.println(users.getMessage());
                                        if (users.getData() instanceof List)
                                            ((List<?>) users.getData()).forEach(System.out::println);
                                        break;
                                    case "7":
                                        oos.writeObject(new Request(RequestAction.LOGOUT, null));
                                        oos.flush();
                                        System.out.println(((Response) ois.readObject()).getMessage());
                                        loggedIn = false;
                                        break;
                                    default:
                                        System.out.println("Unknown option");
                                }
                            }
                        }
                        break;

                    case "3":
                        oos.writeObject(new Request(RequestAction.EXIT, null));
                        oos.flush();
                        Response ex = (Response) ois.readObject();
                        System.out.println(ex.getMessage());
                        running = false;
                        break;

                    default:
                        System.out.println("Unknown choice");
                }
            }

        } catch (Exception e) {
            // TODO: handle exception
            System.err.println("Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
