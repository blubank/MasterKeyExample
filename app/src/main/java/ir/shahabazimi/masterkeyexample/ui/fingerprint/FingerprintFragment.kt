package ir.shahabazimi.masterkeyexample.ui.fingerprint

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import ir.shahabazimi.masterkeyexample.data.AuthenticateResultType
import ir.shahabazimi.masterkeyexample.databinding.FragmentFingerprintBinding
import ir.shahabazimi.masterkeyexample.ui.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * @Author: Shahab Azimi
 * @Date: 2024 - 09 - 14
 **/
class FingerprintFragment : BaseFragment<FragmentFingerprintBinding>() {

    private val viewModel: FingerprintViewModel by viewModel()

    private val args by navArgs<FingerprintFragmentArgs>()

    override fun bindView(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentFingerprintBinding.inflate(inflater, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeAuthenticateResult()
        initViews()
    }

    private fun initViews() = with(binding) {
        button2.setOnClickListener {
            navigateToHub()
        }

        button1.setOnClickListener {
            viewModel.authenticate(args.password.orEmpty())
        }
    }

    private fun navigateToHub(password: String = args.password.orEmpty()) {
        findNavController().navigate(
            FingerprintFragmentDirections.actionFingerprintFragmentToHubFragment(
                password = password
            )
        )
    }

    private fun observeAuthenticateResult() =
        viewModel.authenticateResult.observe(viewLifecycleOwner) {
            when (it.result) {
                AuthenticateResultType.SUCCESS -> navigateToHub(it.data)
                AuthenticateResultType.CANCELED -> Unit
                AuthenticateResultType.ERROR -> Toast.makeText(context, it.data, Toast.LENGTH_SHORT)
                    .show()

                AuthenticateResultType.REMOVED_KEY -> {
                    Toast.makeText(context, it.data, Toast.LENGTH_SHORT).show()
                    navigateToHub()
                }
            }

        }

}