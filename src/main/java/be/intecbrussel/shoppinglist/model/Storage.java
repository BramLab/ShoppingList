package be.intecbrussel.shoppinglist.model;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.*;

@Transactional
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Data //Combines @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
@Entity
@ToString(callSuper=true)
public class Storage {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;
    private String name;
    private String remarks;
}

// stockpiling ("prepping")
//Dry foods: rice (e.g. basmati rice, jasmine rice or brown rice), pasta, pulses
//ingeblikt: Canned food: strained and/or chopped tomatoes, corn, peas, beans, tuna
//Provisions in jars: pesto, olives, red cabbage, sprouts and other vegetables
//Spices: Salt, pepper, paprika spice, vegetable stock, tomato paste
//Liquids: Water, vinegar, oil, UHT milk
//Baking ingredients: Sugar, flour, baking powder, vanilla sugar, dry yeast if necessary
//Other: bread, coffee, cornflakes, jam, honey, margarine, sauce thickener

//      voorraadkast/pantry, bijkeuken/Utility room, koelkast/refrigerator, vriezer/freezer, kelder/cellar
//    , keukenkast/kitchen cupboard, aanrecht/counter
//    , eettafel/dining table, bijzettafel/side table

//Pantry / (Larder): The main location for dry goods, canned food, spices, and grains.
//Kitchen Cupboards / Cabinets: Used for daily items, baking supplies, and canned goods.
//Countertop: Suitable for short-term storage of fruits (bananas, tomatoes), onions, potatoes, and garlic.
//Root Cellar: A cool, dry, dark, and slightly damp place for storing root vegetables (potatoes, carrots, onions) and tubers.
//Storage Room / Basement: A cool, dark, and dry area for long-term storage or bulk purchases.

//4. Specialized/Alternative Storage
//    Garage / Utility Room: Often used for extra dry goods or a chest freezer, but must be checked for temperature extremes.
//    Under-bed / Closet: Used in households with limited space for non-perishable emergency food supplies.
//    Cabinets above the fridge: Often used for long-term storage of items, though they can become warmer.
//    Wine Cellar/Cooler: For wines and some fermented foods.
//
//5. Food Management System Data Structures
//    In a digital system, these are often categorized for easier inventorying:
//    Location Hierarchy: Home -> Kitchen -> Pantry / Fridge / Freezer.
//    Tagging: Using tags like "perishable," "dry," "frozen," or "raw" to automatically map items to the right storage location.

