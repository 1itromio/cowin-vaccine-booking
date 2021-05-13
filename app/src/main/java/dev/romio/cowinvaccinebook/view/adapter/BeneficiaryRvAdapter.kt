package dev.romio.cowinvaccinebook.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.romio.cowinvaccinebook.R
import dev.romio.cowinvaccinebook.data.model.BeneficiarySummary
import dev.romio.cowinvaccinebook.databinding.ItemBeneficiaryBinding

class BeneficiaryRvAdapter: RecyclerView.Adapter<BeneficiaryRvAdapter.BeneficiaryViewHolder>() {

    private val beneficiaries = mutableListOf<BeneficiarySummary>()
    private var enableCheckBoxes = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeneficiaryViewHolder {
        val binding = DataBindingUtil.inflate<ItemBeneficiaryBinding>(LayoutInflater.from(parent.context),
            R.layout.item_beneficiary, parent, false)
        return BeneficiaryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BeneficiaryViewHolder, position: Int) {
        holder.bind(beneficiaries[position])
    }

    override fun getItemCount(): Int = beneficiaries.size

    fun setBeneficiaries(beneficiaries: List<BeneficiarySummary>) {
        val diffUtilCallback = BeneficiaryDiffCallBack(this.beneficiaries, beneficiaries)
        val diffResult = DiffUtil.calculateDiff(diffUtilCallback)
        this.beneficiaries.clear()
        this.beneficiaries.addAll(beneficiaries)
        diffResult.dispatchUpdatesTo(this)
    }

    fun enableChecking(enable: Boolean) {
        this.enableCheckBoxes = enable
        notifyDataSetChanged()
    }


    inner class BeneficiaryViewHolder(
        private val binding: ItemBeneficiaryBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(beneficiarySummary: BeneficiarySummary) {
            binding.cbBeneficiary.setOnCheckedChangeListener { _, isChecked ->
                beneficiarySummary.isChecked = isChecked
            }
            binding.viewmodel = beneficiarySummary
            binding.executePendingBindings()
            binding.cbBeneficiary.isEnabled = enableCheckBoxes
        }
    }
}

class BeneficiaryDiffCallBack(
    private val oldList: List<BeneficiarySummary>,
    private val newList: List<BeneficiarySummary>
): DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].brId == newList[newItemPosition].brId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        return oldItem.brId == newItem.brId &&
                oldItem.isChecked == newItem.isChecked &&
                oldItem.name == newItem.name &&
                oldItem.vaccinationStatus == newItem.vaccinationStatus &&
                oldItem.vaccine == newItem.vaccine
    }

}