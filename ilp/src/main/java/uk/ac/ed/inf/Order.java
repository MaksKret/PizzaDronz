package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.ac.ed.inf.Exceptions.*;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static java.util.List.of;

/**
 * Represents an Order taken by the customer. Additionally, used to pull Orders from a REST-server and de-serialize them, and validate individual Order object.
 */
public class Order {

    /**
     * Constant delivery fee applied to every order.
     */
    private static final int DELIVERY_FEE = 100;

    /**
     * Unique Order Number for the Order.
     */
    @JsonProperty("orderNo")
    private String orderNo;

    /**
     * Date for which the Order was placed.
     */
    @JsonProperty("orderDate")
    private String orderDate;

    /**
     * Name of the customer who made the Order.
     */
    @JsonProperty("customer")
    private String customer;

    /**
     * Number of the Credit card used to pay for the Order.
     */
    @JsonProperty("creditCardNumber")
    private String creditCardNumber;

    /**
     * Expiry date of the Credit card used to pay for the Order.
     */
    @JsonProperty("creditCardExpiry")
    private String creditCardExpiry;

    /**
     * CVV of the Credit card used to pay for the Order.
     */
    @JsonProperty("cvv")
    private String cvv;

    /**
     * Assumed total price for the order.
     */
    @JsonProperty("priceTotalInPence")
    private int priceTotalInPence;

    /**
     * Pizza names ordered by the customer.
     */
    @JsonProperty("orderItems")
    private String[] orderItems;

    /**
     * Outcome of the Order.
     */
    private OrderOutcome orderOutcome;

    /**
     * Name of the participating Restaurant (if order is valid) from which the customer's ordered items come from.
     */
    private String restaurantName;

    /**
     * Method accesses the REST-server, de-serializes it into a List of type Order and validates the
     * orders.
     *
     * @param baseServerURL  String of the Base REST server address, like: "https://website.net/"
     * @param date           String of a date.
     * @param restaurants    List of participating restaurants.
     * @return               An ArrayList object of type Order.
     */
    public static List<Order> getOrdersByDate(String baseServerURL, String date, List<Restaurant> restaurants){

        try {
            var url = new URL(baseServerURL + "orders/" + date);
            Order[] response = new ObjectMapper().readValue(url, Order[].class);
            var rawOrderList = new ArrayList<Order>(Arrays.asList(response));

            // "ValidButNotDelivered" is set as the Default case for all Orders
            for(Order order: rawOrderList){
                order.orderOutcome = OrderOutcome.ValidButNotDelivered;
            }

            // Validates orders, will update all "ValidButNotDelivered" in case an order is Invalid
            return validateOrders(restaurants, rawOrderList);

        } catch (IOException e) {
            // In case an Exception was caught it will return an empty list of type Order.
            return new ArrayList<Order>();
        }
    }

    /**
     * Finds and returns the price of a given pizza from a given menu.
     *
     * @param menu            Menu of a Restaurant object.
     * @param pizza           Name of a pizza.
     * @return                Price of a corresponding pizza in the Menu. 0 otherwise.
     */
    private static int getMenuItemPrice(Menu[] menu, String pizza) {

        for (Menu value : menu) {
            if (value.getName().equals(pizza)) {
                return value.getPriceInPence();
            }
        }
        // returns 0 in case Pizza not found
        return 0;
    }

    /** Calculates the cost of a particular order, given a list of names of menu items.
     *
     * @param restaurants    Array of participating restaurants.
     * @return               Integer representing a total price in pence of the order, based on the Order's items specified,
     *                       with a constant delivery charge of £1 added.
     *
     * @throws InvalidPizzaCombinationException  When the names of Menu items are not strictly from 1 restaurant.
     * @throws NoItemsInOrderException           When the order contains no items.
     * @throws TooManyItemsInOrderException      When the order contains more than 4 items.
     * @throws PizzaNotFoundException            When some item in the Order does not appear in any Restaurant's menu.
     */
    private int getDeliveryCost(List<Restaurant> restaurants) throws InvalidPizzaCombinationException, NoItemsInOrderException, TooManyItemsInOrderException, PizzaNotFoundException {

        if(this.orderItems.length == 0){
            throw new NoItemsInOrderException("No pizzas in the order.");
        }

        if(this.orderItems.length > 4){
            throw new TooManyItemsInOrderException("Too many items in the order.");
        }

        // this checks whether all order items are in some Restaurant's menu

        Restaurant orderRestaurant = null;
        // all available items across all restaurants
        var allAvailableItems = new HashSet<String>();
        List<String> orderItems = of(this.orderItems);

        for (Restaurant restaurant: restaurants){

            // Names of Restaurant's Menu items
            var menuNames = new HashSet<String>();

            Arrays.stream(restaurant.getMenuItems()).toList().forEach(menuItem -> menuNames.add(menuItem.getName()));
            Arrays.stream(restaurant.getMenuItems()).toList().forEach(menuItem -> allAvailableItems.add(menuItem.getName()));

            // checks if given Restaurant's menu contains all items in the order
            if (menuNames.containsAll(orderItems)) {
                orderRestaurant = restaurant;
                break;
            }
        }

        // If Order's items are not available in any one Restaurant, check if all of them exist from the pool of all
        // available Menu items across all restaurants
        if (orderRestaurant == null){
            if (allAvailableItems.containsAll(orderItems)){
                throw new InvalidPizzaCombinationException("Order across multiple restaurants.");
            } else {
                throw new PizzaNotFoundException("Pizza not found");
            }
        }

        int totalPrice = 0;

        for (String orderItem : this.orderItems) {
            int itemPrice = getMenuItemPrice(orderRestaurant.getMenuItems(), orderItem);
            totalPrice += itemPrice;
        }

        // only when all ordered items are valid and come from the same restaurant is the Restaurant name assigned to Order.
        this.restaurantName = orderRestaurant.getName();
        return totalPrice + DELIVERY_FEE;
    }

    /**
     * Verifies that a given expiry date is after the Order's date. Essentially verifies that the expiry date on the order is valid.
     *
     * @param expiryDate      Expiry date of a payment card.
     * @param orderDate       Date of the order.
     * @return                true when expiry date of the card is after the order's date (Card is valid)
     */
    private static boolean isCardExpiryAfterOrderDate(String expiryDate, String orderDate){

        // verifies that expiryDate is in correct format
        if(! expiryDate.matches("\\b(0[1-9]|1[0-2])+/+([0-9][0-9])\\b")){
            return false;
        }

        int mm = Integer.parseInt(expiryDate.charAt(0) + "" + expiryDate.charAt(1));
        int yyyy = Integer.parseInt("20" + expiryDate.charAt(3) + "" + expiryDate.charAt(4));
        LocalDate expiry = LocalDate.of(yyyy, mm, 1);

        return LocalDate.parse(orderDate).isBefore(expiry);

    }

    /**
     * Verifies that the order number of the Order is valid.
     */
    private void isValidOrderNumber() {

        boolean isValid = this.orderNo.length() == 8;
        if (!isValid){
            this.orderOutcome = OrderOutcome.Invalid;
        }
    }

    /**
     * Verifies the Credit Card number of the credit card used to pay for the Order.
     */
    private void isValidCreditCardNumber() {

        boolean isValid = this.creditCardNumber.length() == 16;
        if (!isValid){
            this.orderOutcome = OrderOutcome.InvalidCardNumber;
        }
    }

    /**
     * Verifies that the expiry date of the credit card used to pay for the Order is valid.
     */
    private void isValidCreditCardExpiry() {

        boolean isValid = isCardExpiryAfterOrderDate(this.creditCardExpiry, this.orderDate);
        if (!isValid){
            this.orderOutcome = OrderOutcome.InvalidExpiryDate;
        }
    }

    /**
     * Verifies the Card Verification Value of the credit card used to pay for the order.
     */
    private void isValidCVV() {

        boolean isValid = this.cvv.length() == 3;
        if (!isValid){
            this.orderOutcome = OrderOutcome.InvalidCvv;
        }
    }

    /**
     * Verifies that the provided price for the Order is equal to the combined price of the all items listed
     * in a Restaurant's menu.
     */
    private void isValidPrice(List<Restaurant> restaurants) {

        try {
            boolean isValid = this.priceTotalInPence == getDeliveryCost(restaurants);
            if (!isValid) {
                this.orderOutcome = OrderOutcome.InvalidTotal;
            }
        } catch (TooManyItemsInOrderException ex) {
            this.orderOutcome = OrderOutcome.InvalidPizzaCountTooMany;
        } catch (NoItemsInOrderException ex) {
            this.orderOutcome = OrderOutcome.InvalidNoPizzasInOrder;
        } catch (InvalidPizzaCombinationException ex) {
            this.orderOutcome = OrderOutcome.InvalidPizzaCombinationMultipleSuppliers;
        } catch (PizzaNotFoundException ex){
            this.orderOutcome = OrderOutcome.InvalidPizzaNotDefined;
        }
    }

    /**
     * Validates all the order from a list. For each order, ensures the Order is valid; Items exist in some one given Restaurant, Valid card was used, Order
     * components are Valid.
     *
     * @param restaurants       List of available participating Restaurants.
     * @param orders            List of Orders.
     * @return                  List of Validated Orders.
     */
    private static List<Order> validateOrders(List<Restaurant> restaurants, List<Order> orders) {

        for(Order order: orders){

            order.isValidOrderNumber();
            order.isValidPrice(restaurants);
            order.isValidCVV();
            order.isValidCreditCardNumber();
            order.isValidCreditCardExpiry();
        }
        return orders;
    }


    ////////////////////////
    ///      SETTERS     ///
    ////////////////////////

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public void setOrderDate(String orderDate){
        this.orderDate = orderDate;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public void setCreditCardExpiry(String creditCardExpiry) {
        this.creditCardExpiry = creditCardExpiry;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public void setPriceTotalInPence(int priceTotalInPence) {
        this.priceTotalInPence = priceTotalInPence;
    }

    public void setOrderItems(String[] orderItems) {
        this.orderItems = orderItems;
    }

    public void setOrderOutcome(OrderOutcome orderOutcome) {
        this.orderOutcome = orderOutcome;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    ////////////////////////
    ///      GETTERS     ///
    ////////////////////////

    public String getOrderNo() {
        return orderNo;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public String getCreditCardExpiry() {
        return creditCardExpiry;
    }

    public String getCvv() {
        return cvv;
    }

    public int getPriceTotalInPence() {
        return priceTotalInPence;
    }

    public String[] getOrderItems() {
        return orderItems;
    }

    public OrderOutcome getOrderOutcome() {
        return orderOutcome;
    }

    public String getRestaurantName() {
        return restaurantName;
    }
}
