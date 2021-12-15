package client.jfx;

import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

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
import shared.Order;
import shared.Stock;

public class HomeWindowController implements Initializable {
    private static HomeWindowController inst = null;
    public static final CountDownLatch latch = new CountDownLatch(1);

   @FXML private TextField filterField;
   @FXML private TextField setPrice;
   @FXML private TableView<Stock> tableview;
   @FXML private TableView<Order> sellTable;
   @FXML private TableView<Order> buyTable;
   @FXML private TableColumn<Stock, String> name;
   @FXML private TableColumn<Stock, String> price;
   @FXML private TableColumn<Order,String> sellOrders;
   @FXML private TableColumn<Order,String> buyOrders;
   @FXML private Text stockName;
   @FXML private Text recommended;
   @FXML private Text companyName;
   @FXML private Text companyMC;
   @FXML private Button buyButton;
   @FXML private Button sellButton;

   private static ObservableList<Stock> dataList = FXCollections.observableArrayList();
   private ObservableList<Order> buyList = FXCollections.observableArrayList();
   private ObservableList<Order> sellList = FXCollections.observableArrayList();
   enum Technical {
        NEUTRAL,
        BUY,
        SELL;

        public static String getRandomTechnical(){
            Random random = new Random();
            return values()[random.nextInt(values().length)].toString();
        }
   }

   @Override
   public void initialize(URL url, ResourceBundle rb){
       name.setCellValueFactory(new PropertyValueFactory<>("name"));
       price.setCellValueFactory(new PropertyValueFactory<>("price"));
       sellOrders.setCellValueFactory(new PropertyValueFactory<>("price"));
       buyOrders.setCellValueFactory(new PropertyValueFactory<>("price"));
       Stock stock1 = new Stock("ALPHABET_A", "30", "Google","2000");
       Stock stock2 = new Stock("ALPHABET_B", "20", "Google","3000");
       Stock stock3 = new Stock("MSFT", "25", "Microsoft","4000");
       Stock stock4 = new Stock("AAPL", "40", "Apple","5000");
       Stock stock5 = new Stock("JNJ", "50", "Johnson and Johnson","6000");
       Stock stock6 = new Stock("JPM", "60", "JPMorgan","7000");

       dataList.addAll(stock1, stock2, stock3,stock4,stock5,stock6);

       FilteredList<Stock> filteredData = new FilteredList<>(dataList);

       filterField.textProperty().addListener((observable, oldValue, newValue)->{
           filteredData.setPredicate(stock->{
               if(newValue == null || newValue.isEmpty()){
                   return true;
               }
                String lowerCaseFilter = newValue.toLowerCase();
               if(stock.getName().toLowerCase().indexOf(lowerCaseFilter) != -1){
                   return true;
               }
               else if (stock.getPrice().indexOf(lowerCaseFilter) != -1){
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

       tableview.setOnMouseClicked((MouseEvent event)->{
           if(event.getClickCount() > 0){
               showDetailedStockData();
           }
       });

       buyButton.setOnMouseClicked((MouseEvent event)->{
           if(event.getClickCount() > 0) {
               buyStock();
           }
       });
       sellButton.setOnMouseClicked((MouseEvent event)->{
           if(event.getClickCount() > 0) {
               sellStock();
           }
       });
       inst = this;
       latch.countDown();
   }

   public void showDetailedStockData(){
       if (tableview.getSelectionModel().getSelectedItem() != null) {
           Stock stock = tableview.getSelectionModel().getSelectedItem();
           stockName.setText(stock.getName());
           recommended.setText(Technical.getRandomTechnical());
           companyName.setText(stock.getCompanyName());
           companyMC.setText(stock.getMarketcap());
       }
   }

   public void buyStock() {
       int price = Integer.parseInt(setPrice.getText());
       if (tableview.getSelectionModel().getSelectedItem() != null) {
           Stock stock = tableview.getSelectionModel().getSelectedItem();
           if(price > Integer.parseInt(stock.getPrice())){
               Alert buy_alert = new Alert(Alert.AlertType.INFORMATION,"Price needs to be lower than stock price for buy order");
               buy_alert.show();
           }
           else{
               System.out.println("bought " + stock.getName() + " for " + price);
               buyList.add(new Order(setPrice.getText()));

           }
       }
       else{
           Alert stock_selection_alert = new Alert(Alert.AlertType.INFORMATION,"You did not select a stock");
           stock_selection_alert.show();
       }
   }

   public void sellStock(){
       int price = Integer.parseInt(setPrice.getText());
       if (tableview.getSelectionModel().getSelectedItem() != null) {
           Stock stock = tableview.getSelectionModel().getSelectedItem();
           if(price < Integer.parseInt(stock.getPrice())){
               Alert sell_alert = new Alert(Alert.AlertType.INFORMATION,"Price needs to be higher than stock price for sell order");
               sell_alert.show();
           }
           else{
               System.out.println("sold " + stock.getName() + " for " + price);
               sellList.add(new Order(setPrice.getText()));

           }
       }
       else{
           Alert stock_selection_alert = new Alert(Alert.AlertType.INFORMATION,"You did not select a stock");
           stock_selection_alert.show();
       }
   }

    public static ArrayList<Stock> decodeUpdateMessage(String message) {
        return null;
    }

   public static void updateStocks(String message) {
       decodeUpdateMessage(message);
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
}
