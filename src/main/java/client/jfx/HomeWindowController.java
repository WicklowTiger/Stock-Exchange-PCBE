package client.jfx;

import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import client.ClientActionsManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
    private TableColumn<Order, String> sellOrders;
    @FXML
    private TableColumn<Order, String> buyOrders;
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

    private static final ObservableList<Stock> dataList = FXCollections.observableArrayList();
    private ObservableList<Order> buyList = FXCollections.observableArrayList();
    private ObservableList<Order> sellList = FXCollections.observableArrayList();

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
        sellOrders.setCellValueFactory(new PropertyValueFactory<>("price"));
        buyOrders.setCellValueFactory(new PropertyValueFactory<>("price"));
        Stock stock1 = new Stock("ALPHABET_A", "30", "Google", "2000");
        Stock stock2 = new Stock("ALPHABET_B", "20", "Google", "3000");
        Stock stock3 = new Stock("MSFT", "25", "Microsoft", "4000");
        Stock stock4 = new Stock("AAPL", "40", "Apple", "5000");
        Stock stock5 = new Stock("JNJ", "50", "Johnson and Johnson", "6000");
        Stock stock6 = new Stock("JPM", "60", "JPMorgan", "7000");

        dataList.addAll(stock1, stock2, stock3, stock4, stock5, stock6);

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

        tableview.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() > 0) {
                showDetailedStockData();
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
        if (tableview.getSelectionModel().getSelectedItem() != null) {
            Stock stock = tableview.getSelectionModel().getSelectedItem();
            stockName.setText(stock.getName());
            recommended.setText(Technical.getRandomTechnical());
            companyName.setText(stock.getCompanyName());
            companyMC.setText(stock.getMarketcap());
        }
    }

    public void buyStock(float amount, float price) {
        String orderType = (String) typeCombo.getValue();
        if (tableview.getSelectionModel().getSelectedItem() != null) {
            Stock stock = tableview.getSelectionModel().getSelectedItem();
            if (orderType.toString().toLowerCase().equals("limit")) {
                if (price > Integer.parseInt(stock.getPrice())) {
                    Alert sell_alert = new Alert(Alert.AlertType.INFORMATION, "Price needs to be lower than stock price for sell order");
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
                if (price < Integer.parseInt(stock.getPrice())) {
                    Alert sell_alert = new Alert(Alert.AlertType.INFORMATION, "Price needs to be higher than stock price for sell order");
                    sell_alert.show();
                } else {
                    ClientActionsManager.putAction(new Action(ActionType.SEND_SELL, stock.getName() + "," + amount + "," + price));

                }
            } else {
                System.out.println(orderType + " SELL " + stock.getName() + " " + amount + "@" + stock.getPrice());
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
            stockList.add(new Stock(stock_params[0], stock_params[1]));
        }
        return stockList;
    }

    public static void updateStocks(String message) {
        //check if stock exists; if yes, update existing stocks; if not, add it at the end of the datalist
        ObservableList<Stock> auxDataList = FXCollections.observableArrayList();
        dataList.forEach(stock -> {
            auxDataList.add(stock);
        });

        ArrayList<Stock> incomingStocksList = decodeUpdateMessage(message);
        incomingStocksList.forEach(incomingStock -> {
            AtomicBoolean stockFound = new AtomicBoolean(false);
            auxDataList.forEach(existingStock -> {
                if (existingStock.getName().equals(incomingStock.getName())) {
                    existingStock.setPrice(incomingStock.getPrice());
                    stockFound.set(true);
                }
            });
            if (stockFound.get() == false) {
                auxDataList.add(incomingStock);
            }

        });
        dataList.clear();
        auxDataList.forEach(stock -> {
            dataList.add(stock);
        });
    }

    public static void addStock(Stock stock) {
        dataList.add(stock);
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
}
