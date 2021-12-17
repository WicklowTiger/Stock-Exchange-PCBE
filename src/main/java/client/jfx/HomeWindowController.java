package client.jfx;

import java.net.URL;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import client.ClientActionsManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import shared.*;

public class HomeWindowController implements Initializable {
    private static HomeWindowController inst = null;
    public static final CountDownLatch latch = new CountDownLatch(1);

    @FXML
    private TextField filterField;
    @FXML
    private TextField setPrice;
    @FXML
    private TextField setAmount;
    @FXML
    private TableView<Stock> tableview;
    @FXML
    private TableView<Order> sellTable;
    @FXML
    private TableView<Order> buyTable;
    @FXML
    private TableColumn<Stock, String> name;
    @FXML
    private TableColumn<Stock, String> price;
    @FXML
    private TableColumn<Order, String> sellOrdersPrice;
    @FXML
    private TableColumn<Order, String> sellOrdersAmount;
    @FXML
    private TableColumn<Order, String> buyOrdersPrice;
    @FXML
    private TableColumn<Order, String> buyOrdersAmount;
    @FXML
    private Text stockName;
    @FXML
    private Text recommended;
    @FXML
    private Text companyName;
    @FXML
    private Text companyMC;
    @FXML
    private Button buyButton;
    @FXML
    private Button sellButton;
    @FXML
    private ComboBox typeCombo;
    @FXML
    private Text priceLabel;
    @FXML
    private TableView<FxOrder> myOrders;
    @FXML
    private TableColumn<FxOrder,String> myName;
    @FXML
    private TableColumn<FxOrder,String> myType;
    @FXML
    private TableColumn<FxOrder,String> myPrice;
    @FXML
    private TableColumn<FxOrder,String> myAmount;
    @FXML
    private Text userBalance;
    @FXML
    private Text stockBalance;
    @FXML
    private Text currentPrice;

    private static final ObservableList<Stock> dataList = FXCollections.observableArrayList();
    private static ObservableList<Order> buyList = FXCollections.observableArrayList();
    private static ObservableList<Order> sellList = FXCollections.observableArrayList();
    private static ObservableList<FxOrder> myOrderList = FXCollections.observableArrayList();
    private static Map<String,Float> fxStockBalance = new HashMap<>();

    enum Technical {
        NEUTRAL,
        BUY,
        SELL;

        public static String getRandomTechnical() {
            Random random = new Random();
            return values()[random.nextInt(values().length)].toString();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        typeCombo.getSelectionModel().select(1);
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        price.setCellValueFactory(new PropertyValueFactory<>("price"));
        sellOrdersPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        buyOrdersPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        sellOrdersAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        buyOrdersAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        myName.setCellValueFactory(new PropertyValueFactory<>("stockName"));
        myType.setCellValueFactory(new PropertyValueFactory<>("orderType"));
        myPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        myAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));


        FilteredList<Stock> filteredData = new FilteredList<>(dataList);

        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(stock -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                if (stock.getName().toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true;
                } else if (stock.getPrice().indexOf(lowerCaseFilter) != -1) {
                    return true;
                }
                return false;
            });
        });

        SortedList<Stock> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableview.comparatorProperty());

        tableview.setItems(sortedData);

        sellTable.setItems(sellList);
        buyTable.setItems(buyList);
        myOrders.setItems(myOrderList);

        tableview.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() > 0) {
                if(inst.tableview.getSelectionModel().getSelectedItem()!=null){
                    this.selectStock();
                }

            }
        });

        buyButton.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() > 0) {
                handleTradeAction(ActionType.SEND_BUY);
            }
        });
        sellButton.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() > 0) {
                handleTradeAction(ActionType.SEND_SELL);
            }
        });

        typeCombo.getSelectionModel().selectedItemProperty().addListener((options, oldvalue, newvalue) -> {
            if (newvalue.toString().toLowerCase().equals("market")) {
                setPrice.setVisible(false);
                priceLabel.setVisible(false);
            } else {
                setPrice.setVisible(true);
                priceLabel.setVisible(true);
            }
        });

        inst = this;
        latch.countDown();
    }

    public void showDetailedStockData() {
        Stock stock = tableview.getSelectionModel().getSelectedItem();
        stockName.setText(stock.getName());
        recommended.setText(Technical.getRandomTechnical());
        companyName.setText(stock.getCompanyName());
        companyMC.setText(stock.getMarketCap());
        currentPrice.setText(stock.getPrice().toString());
    }

    public void buyStock(float amount, float price) {
        String orderType = (String) typeCombo.getValue();
        if (tableview.getSelectionModel().getSelectedItem() != null) {
            Stock stock = tableview.getSelectionModel().getSelectedItem();
            if (orderType.toString().toLowerCase().equals("limit")) {
                if (price > stock.price) {
                    Alert sell_alert = new Alert(Alert.AlertType.INFORMATION, "Price needs to be lower than stock price for buy order");
                    sell_alert.show();
                } else {
                    ClientActionsManager.putAction(new Action(ActionType.SEND_BUY, stock.getName() + "," + amount + "," + price));
                }
            } else {
                ClientActionsManager.putAction(new Action(ActionType.SEND_BUY, stock.getName() + "," + amount));
            }
        } else {
            Alert stock_selection_alert = new Alert(Alert.AlertType.INFORMATION, "You did not select a stock");
            stock_selection_alert.show();
        }
    }

    public void buyStock(float amount) {
        System.out.println("hello");
        this.buyStock(amount, -1f);
    }

    public void sellStock(float amount, float price) {
        String orderType = (String) typeCombo.getValue();
        if (tableview.getSelectionModel().getSelectedItem() != null) {
            Stock stock = tableview.getSelectionModel().getSelectedItem();
            if (orderType.toString().toLowerCase().equals("limit")) {
                if (price < stock.price) {
                    Alert sell_alert = new Alert(Alert.AlertType.INFORMATION, "Price needs to be higher than stock price for sell order");
                    sell_alert.show();
                } else {
                    ClientActionsManager.putAction(new Action(ActionType.SEND_SELL, stock.getName() + "," + amount + "," + price));

                }
            } else {
                ClientActionsManager.putAction(new Action(ActionType.SEND_SELL, stock.getName() + "," + amount));
            }

        } else {
            Alert stock_selection_alert = new Alert(Alert.AlertType.INFORMATION, "You did not select a stock");
            stock_selection_alert.show();
        }
    }

    public void sellStock(float amount) {
        this.sellStock(amount, -1f);
    }

    public void handleTradeAction(ActionType actionType) {
        if (!this.checkInputFields()) {
            return;
        }
        float price = -1f, amount = -1f;
        String orderType = (String) typeCombo.getValue();
        switch (orderType.toLowerCase()) {
            case "limit":
                try {
                    price = Float.parseFloat(setPrice.getText());
                    amount = Float.parseFloat(setAmount.getText());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (actionType == ActionType.SEND_BUY) {
                    buyStock(amount, price);
                } else if (actionType == ActionType.SEND_SELL) {
                    sellStock(amount, price);
                }
                break;
            case "market":
                try {
                    amount = Float.parseFloat(setAmount.getText());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (actionType == ActionType.SEND_BUY) {
                    buyStock(amount);
                } else if (actionType == ActionType.SEND_SELL) {
                    sellStock(amount);
                }
                break;
            default:
                break;
        }
    }

    private static ArrayList<Stock> decodeUpdateMessage(String message) {
        //AAPL,35;MSFT,25
        ArrayList<Stock> stockList = new ArrayList<Stock>();
        String[] message_list = message.split(";");
        for (int i = 0; i < message_list.length; i++) {
            String[] stock_params = message_list[i].split(",");
            if(stock_params.length != 2) { break; }
            stockList.add(new Stock(stock_params[0], Float.parseFloat(stock_params[1])));
        }
        return stockList;
    }

    public static void updateStocks(String message) {
        //get the item that was selected before
        String[] tempArr = message.split("!ORDERS!");

        int selectedPosition = -1;
        Stock selectedStock = inst.tableview.getSelectionModel().getSelectedItem();

        if(selectedStock!=null){
            if(dataList.size()!=0){
                for(int i=0;i<dataList.size();i++){
                    if(dataList.get(i).getName().equals(selectedStock.getName())){
                        selectedPosition = i;
                        break;
                    }
                }
            }
        }

        //check if stock exists; if yes, update existing stocks; if not, add it at the end of the datalist
        ObservableList<Stock> auxDataList = FXCollections.observableArrayList();
        auxDataList.addAll(dataList);

        ArrayList<Stock> incomingStocksList = decodeUpdateMessage(tempArr[0]);
        incomingStocksList.forEach(incomingStock -> {
            AtomicBoolean stockFound = new AtomicBoolean(false);
            auxDataList.forEach(existingStock -> {
                if (existingStock.getName().equals(incomingStock.getName())) {
                    existingStock.setPrice(incomingStock.price);
                    stockFound.set(true);
                }
            });
            if (!stockFound.get()) {
                auxDataList.add(incomingStock);
            }

        });
        dataList.clear();
        dataList.addAll(auxDataList);
        if(selectedPosition > -1) {
            inst.tableview.getSelectionModel().select(selectedPosition);
            inst.tableview.getFocusModel().focus(selectedPosition);
        }
        updateOrders(tempArr[1]);
    }

    public static void openDialogBox(String message) {
        Platform.runLater(() -> {
            new Alert(Alert.AlertType.INFORMATION, message).show();
        });
    }

    public static HomeWindowController getInstance() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return inst;
    }

    private boolean checkInputFields() {
        String orderType = (String) typeCombo.getValue();
        switch (orderType) {
            case "limit":
                if (setPrice.getText().equals("") || setAmount.getText().equals("")) {
                    return false;
                }
                break;
            case "market":
                if (setAmount.getText().equals("")) {
                    return false;
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void selectStock() {
        if (tableview.getSelectionModel().getSelectedItem() == null) { return; }
        this.showDetailedStockData();
        sellList.clear();
        buyList.clear();
        dataList.forEach(stock->{
            if(stock.getName().equals(inst.tableview.getSelectionModel().getSelectedItem().getName())){
                sellList.addAll(stock.sellOrders);
                buyList.addAll(stock.buyOrders);
            }
        });
        //Update stock balance
        String selectedStockName = inst.tableview.getSelectionModel().getSelectedItem().getName();
        if(fxStockBalance.containsKey(selectedStockName)){
            inst.stockBalance.setText(fxStockBalance.get(selectedStockName).toString());
        }
    }

    public static void decodeOrders(String message){
        //AAPL,B31-0.39,B32-12,B33-70;MSFT,S80-0.5,400-0.7
        dataList.forEach(stock->{
            stock.sellOrders.clear();
            stock.buyOrders.clear();
        });

        String[] ordersList = message.split(";");
        for(int i=0;i<ordersList.length;i++){
            String[] stockData = ordersList[i].split(",");
            String stockName = stockData[0];
            if(stockData.length>1){
                for(int j=1;j<stockData.length;j++){
                    char orderType = stockData[j].charAt(0);
                    stockData[j] = stockData[j].replace("B","");
                    stockData[j] = stockData[j].replace("S","");
                    float price = Float.parseFloat(stockData[j].split("-")[0]);
                    float amount = Float.parseFloat(stockData[j].split("-")[1]);
                    if(orderType=='B'){
                        dataList.forEach(stock->{
                            if(stock.name.equals(stockName)){
                                stock.buyOrders.add(new Order(price, amount,stockName));
                            }
                        });
                    }
                    else{
                        dataList.forEach(stock->{
                            if(stock.name.equals(stockName)){
                                stock.sellOrders.add(new Order(price, amount,stockName));
                            }
                        });
                    }
                }
            }


        }

    }
    public static void updateOrders(String message){
        decodeOrders(message);
        if(inst.tableview.getSelectionModel().getSelectedItem()==null) { return; }
        sellList.clear();
        buyList.clear();
        dataList.forEach(stock->{
            if(stock.getName().equals(inst.tableview.getSelectionModel().getSelectedItem().getName())){
                sellList.addAll(stock.sellOrders);
                buyList.addAll(stock.buyOrders);
            }
        });
    }

    public static void updateUser(User user){
        Platform.runLater(() -> {
            //Update user balance
            inst.userBalance.setText(user.balance.toString());
            //Update stock balance for each stock
            fxStockBalance.putAll(user.stockBalance);
            //Update user's order list
            myOrderList.clear();
            user.buyOrders.forEach(buyOrder->{
                myOrderList.add(new FxOrder(buyOrder,buyOrder.stockName,"BUY"));
            });
            user.sellOrders.forEach(sellOrder->{
                myOrderList.add(new FxOrder(sellOrder,sellOrder.stockName,"SELL"));
            });
            if(inst.tableview.getSelectionModel().getSelectedItem() == null) { return; }
            Stock selectedStock = inst.tableview.getSelectionModel().getSelectedItem();
            if(fxStockBalance.containsKey(selectedStock.name)){
                inst.stockBalance.setText(fxStockBalance.get(selectedStock.name).toString());
            }
            inst.currentPrice.setText(selectedStock.getPrice());
        });

    }

}
