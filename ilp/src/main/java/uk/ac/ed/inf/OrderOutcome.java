package uk.ac.ed.inf;

/**
 * Enum object describing the Outcome of an Order.
 */
public enum OrderOutcome {

    /**
     * Assigned when Order is valid, and Drone was able to deliver it before running out of moves.
     */
    Delivered ,

    /**
     * Assigned when Order is valid but Restaurant from which Order items come from was inaccessible or the Drone ran out of moves before being able to deliver an Order.
     */
    ValidButNotDelivered ,

    /**
     * Assigned when the card number of card used to pay for Order is invalid.
     */
    InvalidCardNumber ,

    /**
     * Assigned when the card expiry date of card used to pay for Order is invalid.
     */
    InvalidExpiryDate ,

    /**
     * Assigned when the card CVV of card used to pay for Order is invalid
     */
    InvalidCvv ,

    /**
     * Assigned when the Order's total does not match the total of all the items from Restaurant's menu with delivery fee added.
     */
    InvalidTotal ,

    /**
     * Ordered pizza is not present in any participating Restaurants.
     */
    InvalidPizzaNotDefined ,

    /**
     * When Order does not specify any Pizzas to be delivered
     */
    InvalidNoPizzasInOrder ,

    /**
     * When Order contains more than 4 items to be delivered.
     */
    InvalidPizzaCountTooMany,

    /**
     * When pizzas ordered do not come from only one Restaurant.
     */
    InvalidPizzaCombinationMultipleSuppliers ,

    /**
     * Assigned when the number of the Order is invalid.
     */
    Invalid
}

