# Business Requirements

Business requirements state what _Munchies_ must achieve for the people and organisations that use it, independently
of how the system is built. They are the starting point for the
[Functional Requirements](functional-requirements.md), which describe the behaviour that satisfies them, and for the
[Non-functional Requirements](non-functional-requirements.md), which describe the qualities that behaviour must have.

## Business context

Ordering food from a restaurant usually goes through the phone or through large delivery marketplaces, which own the
relationship with the customer and give the restaurant little control over its menu, its schedule and its workload.
_Munchies_ is a platform where restaurants publish and manage their own offer, and customers order from them directly,
choosing how and when they want their food.

The main operational problem on the restaurant side is the peak: most orders arrive in a few hours around lunch and
dinner. A kitchen that accepts more orders than it can prepare delivers late, and a platform that slows down during the
peak loses orders at the moment they matter most.

## Stakeholders

| Stakeholder | Interest                                                                                              |
|-------------|-------------------------------------------------------------------------------------------------------|
| Customer    | Find restaurants nearby, order quickly, customize dishes, choose when and how to receive the food     |
| Manager     | Publish and maintain one or more restaurants and their menus, receive orders the kitchen can handle   |
| Platform    | Run the service reliably during peaks, at a cost proportional to the load                             |

A manager is also a customer: every manager can use the platform to order like any other user (see the
[Glossary](glossary.md)).

## Requirements

| ID    | Requirement                                                                                                         | Rationale                                                                                   |
|-------|---------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------|
| BR-1  | Customers can find the restaurants closest to them and order from their menus online.                               | The core value of the platform for customers.                                               |
| BR-2  | An order can be fulfilled by delivery, by takeaway, or by eating at the restaurant (dine-in).                        | Restaurants serve all three channels; one platform must cover them all.                     |
| BR-3  | Customers choose when their order should be ready or delivered.                                                     | Lets customers plan ahead and spreads orders over time instead of concentrating them.       |
| BR-4  | Customers can customize each dish with the variations the restaurant offers.                                        | Customization is part of the restaurant's offer and affects the price.                      |
| BR-5  | Customers eating at the restaurant can reserve a table in advance.                                                  | Completes the dine-in channel.                                                              |
| BR-6  | Customers pay for their orders online.                                                                              | An order is confirmed only once it is paid, so the restaurant does not prepare unpaid food. |
| BR-7  | Managers create and manage their own restaurants: details, business hours, menus, dishes, variations and availability. | Restaurants keep full control of their offer without going through the platform operator.  |
| BR-8  | A manager can run several restaurants from the same account.                                                        | Owners with more than one location are a key segment.                                       |
| BR-9  | Each restaurant accepts only as many orders per time slot as its kitchen can prepare.                               | Prevents late orders during peaks; see the [Capacity Agent](../04-Agent-Based%20Architecture/capacity-agent.md). |
| BR-10 | Customers are kept informed about their account, their payments and the progress of their orders.                   | Reduces uncertainty and support requests.                                                   |
| BR-11 | The platform stays responsive during lunch and dinner peaks, and its running cost follows the load.                 | Peaks are when most revenue is made; idle capacity outside them is wasted.                  |

BR-11 is not a feature but a business driver for the system's qualities. It is refined into measurable targets in the
[Non-functional Requirements](non-functional-requirements.md).
