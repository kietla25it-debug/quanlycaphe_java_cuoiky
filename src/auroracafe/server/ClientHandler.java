package auroracafe.server;

import auroracafe.model.AppData;
import auroracafe.model.Role;
import auroracafe.network.*;
import auroracafe.service.AuthService;
import auroracafe.service.CafeService;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Path;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final AuthService authService;
    private final CafeService cafeService;

    public ClientHandler(Socket socket, AuthService authService, CafeService cafeService) {
        this.socket = socket;
        this.authService = authService;
        this.cafeService = cafeService;
    }

    @Override
    public void run() {
        String remote = socket.getRemoteSocketAddress().toString();
        try (Socket s = socket;
             ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(s.getInputStream())) {
            Object raw = in.readObject();
            if (!(raw instanceof Request req)) {
                out.writeObject(Response.fail("Request không hợp lệ."));
                return;
            }
            ServerLogger.log(remote + " ACTION " + req.getAction());
            out.writeObject(handle(req));
            out.flush();
        } catch (Exception e) {
            ServerLogger.log(remote + " ERROR " + e.getMessage());
        }
    }

    private Response handle(Request req) {
        try {
            synchronized (cafeService.getData()) {
                return switch (req.getAction()) {
                    case "PING" -> Response.ok("PONG");
                    case "LOGIN" -> {
                        LoginRequest login = (LoginRequest) req.getData();
                        yield Response.ok(authService.login(login.username, login.password));
                    }
                    case "REGISTER_PUBLIC" -> {
                        RegisterRequest r = (RegisterRequest) req.getData();
                        String error = authService.registerPublic(r.fullName, r.username, r.password, r.confirmPassword);
                        yield error == null ? Response.ok(true) : Response.fail(error);
                    }
                    case "REGISTER_STAFF" -> {
                        RegisterRequest r = (RegisterRequest) req.getData();
                        Role role = r.role == null ? Role.STAFF : r.role;
                        String error = authService.registerStaff(r.fullName, r.username, r.password, role);
                        yield error == null ? Response.ok(true) : Response.fail(error);
                    }
                    case "AUTH_SERVICE" -> handleAuth((ServiceCallRequest) req.getData());
                    case "CAFE_SERVICE" -> handleCafe((ServiceCallRequest) req.getData());
                    case "GET_APP_DATA" -> Response.ok(cafeService.getData());
                    case "SAVE_APP_DATA" -> Response.fail("Client không được ghi trực tiếp toàn bộ AppData. Hãy gọi nghiệp vụ qua CAFE_SERVICE.");
                    case "EXPORT_MENU_CSV" -> {
                        MenuCsvRequest r = (MenuCsvRequest) req.getData();
                        Path path = cafeService.exportMenuCsv(r.path);
                        yield Response.ok(path.toString());
                    }
                    case "IMPORT_MENU_CSV" -> {
                        MenuCsvRequest r = (MenuCsvRequest) req.getData();
                        int count = cafeService.importMenuCsv(r.path, r.replaceAll);
                        yield Response.ok(count);
                    }
                    default -> Response.fail("Action chưa hỗ trợ: " + req.getAction());
                };
            }
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }


    private Response handleAuth(ServiceCallRequest call) {
        Object[] a = call.getArgs();
        Object result = switch (call.getMethod()) {
            case "login" -> authService.login((String) a[0], (String) a[1]);
            case "recordLogout" -> { authService.recordLogout((auroracafe.model.User) a[0]); yield true; }
            case "getAuthHistory" -> authService.getAuthHistory();
            case "registerPublic" -> authService.registerPublic((String) a[0], (String) a[1], (String) a[2], (String) a[3]);
            case "registerStaff" -> authService.registerStaff((String) a[0], (String) a[1], (String) a[2], (Role) a[3]);
            default -> throw new IllegalArgumentException("Auth method chưa hỗ trợ: " + call.getMethod());
        };
        ServerLogger.log("AUTH_SERVICE " + call.getMethod());
        return Response.ok(result);
    }

    private Response handleCafe(ServiceCallRequest call) {
        Object[] a = call.getArgs();
        Object result = switch (call.getMethod()) {
            case "getData" -> cafeService.getData();
            case "findMenu" -> cafeService.findMenu((String) a[0], (String) a[1]);
            case "getFeaturedMenuItems" -> cafeService.getFeaturedMenuItems();
            case "getCategories" -> cafeService.getCategories();
            case "addMenuItem5" -> cafeService.addMenuItem((String) a[0], (String) a[1], (Double) a[2], (Boolean) a[3], (String) a[4]);
            case "addMenuItem6" -> cafeService.addMenuItem((String) a[0], (String) a[1], (Double) a[2], (Boolean) a[3], (String) a[4], (String) a[5]);
            case "updateMenuItem6" -> { cafeService.updateMenuItem((Integer) a[0], (String) a[1], (String) a[2], (Double) a[3], (Boolean) a[4], (String) a[5]); yield true; }
            case "updateMenuItem7" -> { cafeService.updateMenuItem((Integer) a[0], (String) a[1], (String) a[2], (Double) a[3], (Boolean) a[4], (String) a[5], (String) a[6]); yield true; }
            case "deleteMenuItem" -> { cafeService.deleteMenuItem((Integer) a[0]); yield true; }
            case "getTables" -> cafeService.getTables();
            case "getOccupiedTableCount" -> cafeService.getOccupiedTableCount();
            case "getAvailableTableCount" -> cafeService.getAvailableTableCount();
            case "updateTableStatus" -> { cafeService.updateTableStatus((Integer) a[0], (auroracafe.model.TableStatus) a[1]); yield true; }
            case "openOrder" -> cafeService.openOrder((Integer) a[0], (auroracafe.model.User) a[1], (String) a[2]);
            case "openOrResumeOrder" -> cafeService.openOrResumeOrder((Integer) a[0], (auroracafe.model.User) a[1], (String) a[2]);
            case "getOpenOrderForTable" -> cafeService.getOpenOrderForTable((Integer) a[0]);
            case "addItemToOrder" -> { cafeService.addItemToOrder((Integer) a[0], (auroracafe.model.MenuItem) a[1], (Integer) a[2], (String) a[3]); yield true; }
            case "updateOrderItem" -> { cafeService.updateOrderItem((Integer) a[0], (Integer) a[1], (Integer) a[2], (String) a[3]); yield true; }
            case "removeItem" -> { cafeService.removeItem((Integer) a[0], (Integer) a[1]); yield true; }
            case "releaseTable" -> { cafeService.releaseTable((Integer) a[0]); yield true; }
            case "checkoutOrder" -> cafeService.checkoutOrder((Integer) a[0], (String) a[1], (String) a[2]).toString();
            case "getOrders" -> cafeService.getOrders();
            case "findOrders" -> cafeService.findOrders((String) a[0], (java.time.LocalDate) a[1], (java.time.LocalDate) a[2]);
            case "buildInvoiceText" -> cafeService.buildInvoiceText((auroracafe.model.Order) a[0]);
            case "exportInvoice" -> cafeService.exportInvoice((auroracafe.model.Order) a[0]).toString();
            case "exportMenuCsv" -> cafeService.exportMenuCsv(Path.of((String) a[0])).toString();
            case "importMenuCsv" -> cafeService.importMenuCsv(Path.of((String) a[0]), (Boolean) a[1]);
            case "saveSettings" -> { cafeService.saveSettings((String) a[0], (String) a[1], (Double) a[2], (Double) a[3], (String) a[4]); yield true; }
            case "isClockedIn" -> cafeService.isClockedIn((auroracafe.model.User) a[0]);
            case "clockIn" -> cafeService.clockIn((auroracafe.model.User) a[0]);
            case "clockOut" -> cafeService.clockOut((auroracafe.model.User) a[0]);
            case "getOpenShift" -> cafeService.getOpenShift((auroracafe.model.User) a[0]);
            case "getTodayHoursForUser" -> cafeService.getTodayHoursForUser((auroracafe.model.User) a[0]);
            case "getTodayShiftRecords" -> cafeService.getTodayShiftRecords();
            case "getTodaySalesStats" -> cafeService.getTodaySalesStats();
            case "getDailySalesStats" -> cafeService.getDailySalesStats((Integer) a[0]);
            case "getTopSellingItemsToday" -> cafeService.getTopSellingItemsToday();
            case "getCurrentMonthSalesStats" -> cafeService.getCurrentMonthSalesStats();
            case "canEditFees" -> cafeService.canEditFees((auroracafe.model.User) a[0]);
            case "save" -> { cafeService.save(); yield true; }
            case "findMenuItem" -> cafeService.findMenuItem((Integer) a[0]);
            case "findTable" -> cafeService.findTable((Integer) a[0]);
            case "findOrder" -> cafeService.findOrder((Integer) a[0]);
            case "summaryStats" -> cafeService.summaryStats();
            case "canManageUsers" -> cafeService.canManageUsers((auroracafe.model.User) a[0]);
            default -> throw new IllegalArgumentException("Cafe method chưa hỗ trợ: " + call.getMethod());
        };
        ServerLogger.log("CAFE_SERVICE " + call.getMethod());
        return Response.ok(result);
    }

    private void replaceAppData(AppData target, AppData source) {
        target.getUsers().clear();
        target.getUsers().addAll(source.getUsers());
        target.getMenuItems().clear();
        target.getMenuItems().addAll(source.getMenuItems());
        target.getTables().clear();
        target.getTables().addAll(source.getTables());
        target.getOrders().clear();
        target.getOrders().addAll(source.getOrders());
        target.getShiftRecords().clear();
        target.getShiftRecords().addAll(source.getShiftRecords());
        target.getAuthHistoryRecords().clear();
        target.getAuthHistoryRecords().addAll(source.getAuthHistoryRecords());
        target.setSettings(source.getSettings());
        target.syncCounters();
    }
}
