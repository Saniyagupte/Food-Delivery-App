import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.foodie.foodieapp.R

class IngredientAdapter(
    private val ingredients: List<String>,
    private val initialQuantities: MutableMap<String, Int> = mutableMapOf()
) : RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder>() {

    class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ingredientName: TextView = itemView.findViewById(R.id.ingredientName)
        val decreaseButton: Button = itemView.findViewById(R.id.decreaseButton)
        val ingredientQuantity: TextView = itemView.findViewById(R.id.ingredientQuantity)
        val increaseButton: Button = itemView.findViewById(R.id.increaseButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ingredient_item, parent, false)
        return IngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val ingredient = ingredients[position]

        holder.ingredientName.text = ingredient
        // Initialize the ingredient's quantity to 0 if not set
        val quantity = initialQuantities[ingredient] ?: 0
        holder.ingredientQuantity.text = quantity.toString()

        // Decrease button functionality
        holder.decreaseButton.setOnClickListener {
            var currentQuantity = initialQuantities[ingredient] ?: 0
            if (currentQuantity > 0) {
                currentQuantity--
                initialQuantities[ingredient] = currentQuantity
                holder.ingredientQuantity.text = currentQuantity.toString()
            }
        }

        // Increase button functionality
        holder.increaseButton.setOnClickListener {
            var currentQuantity = initialQuantities[ingredient] ?: 0
            currentQuantity++
            initialQuantities[ingredient] = currentQuantity
            holder.ingredientQuantity.text = currentQuantity.toString()
        }
    }

    override fun getItemCount(): Int {
        return ingredients.size
    }
}
