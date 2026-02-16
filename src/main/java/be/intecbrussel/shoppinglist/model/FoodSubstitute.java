package be.intecbrussel.shoppinglist.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data //Combines @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
@Entity
public class FoodSubstitute extends AuditModel {
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long id;
    private long FoodOriginal;
    private long FoodAlternative;
    private Recipe recipe;

    private String aspect; // substitute is never 1:1. Always for certain aspects.
    // e.g. use herbs instead of salt to eliminate salt, but could also be to eat more specific herbs.
    // Weights also can change: Saffron	replaced by more weight of Turmeric (for color; flavor is different).

    // similarity grid: sweet, sour, bitter, salt, umami, creaminess, greasiness...
    // Substitutes can also be for other reasons: vegan reason: tofu instead of chicken.
    // Medical, religious, convinced, health beliefs, norms and values, ...

    // Creaminess: desirable thick, smooth texture, often described velvety or milky, coats mouth without being heavy.
    // Greasiness: more negative sensation of excess, oily fat, resulting in a slippery, heavy, or lingering residue.

    // tamarind paste: unique sweet-and-sour flavor profile
    // mix of lime/lemon juice and brown sugar
    // pomegranate molasses
    // or dried mango powder (amchur)
    // For a 1:1 ratio, use equal parts lime juice and brown sugar (or honey/maple syrup) to mimic the tangy, fruity taste.
    // Here are the top, actionable substitutes:
    //    Lime/Lemon Juice + Brown Sugar (Best Overall): Combine 1 tablespoon of lime juice and 1 tablespoon of brown sugar for every 1 tablespoon of tamarind paste.
    //    Pomegranate Molasses: A thick, sour, and slightly sweet syrup that is an excellent substitute for tamarind's depth.
    //    Amchur (Dried Mango Powder): Provides a fruity, tangy flavor similar to tamarind, ideal for Indian curries.
    //    Worcestershire Sauce: A good, easily accessible substitute since it actually contains tamarind.
    //    Balsamic or Rice Vinegar: Best used in small amounts to add acidity, preferably mixed with sugar.
    //    Kokum: A popular, very sour, and slightly sweet berry used in Indian cuisine.
    //    Tomato Ketchup: A convenient, though sweeter, alternative that provides similar complexity.
    //
    // Tips for Substitution:
    //    For Curries/Dishes: Use lime/lemon juice combined with brown sugar, or try kokum, which is common in Indian cuisine.
    //    For Pad Thai/Sauces: Use a mix of rice vinegar, sugar, and a little ketchup, or use pomegranate molasses.
    //    Ratio: If using citrus alone, you may need a 2:1 ratio (2 tbsp lime juice for 1 tbsp tamarind paste).
}
