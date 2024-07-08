package com.example.login.ui.decoration

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationSet
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import android.view.animation.TranslateAnimation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.login.R
import com.example.login.RetrofitClient
import com.example.login.databinding.FragmentDecorationBinding
import com.example.login.interfaces.ApiService
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton


class DecorationFragment : Fragment(){
    private var _binding: FragmentDecorationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val imageUrls: MutableList<String> = mutableListOf()
    private lateinit var adapter: RecyclerAdapter
    private lateinit var toggleButton: MaterialButton
    private var isRecyclerViewVisible = true
    private lateinit var recyclerView: RecyclerView
    private val apiService: ApiService by lazy {
        RetrofitClient.getClient(requireContext()).create(ApiService::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDecorationBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.recyclerview
        toggleButton = binding.buttonToggle

        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)


        initRecycler()
        recyclerView.visibility = View.VISIBLE
        isRecyclerViewVisible = true

        toggleButton.setOnClickListener {
            if (isRecyclerViewVisible) {
                slideDown(recyclerView, toggleButton)
            } else {
                slideUp(recyclerView, toggleButton)
            }
            isRecyclerViewVisible = !isRecyclerViewVisible
        }
    }


    private fun initRecycler() {
        imageUrls.add("https://search.pstatic.net/sunny/?src=https%3A%2F%2Fwww.shutterstock.com%2Fimage-vector%2Fthis-minimalist-logo-design-your-260nw-2397842879.jpg&type=a340")
        imageUrls.add("https://search.pstatic.net/common/?src=http%3A%2F%2Fblogfiles.naver.net%2FMjAyNDA2MDRfMTEg%2FMDAxNzE3NDY3OTU0NjQ3.manwzdw7qWGewwwO_hFRmbTUh19i5bw8LyreA2zoYk4g.VsLhsUk4osPUU0IxhF2z1xIXoWOyGJkyFl7C0Okkrb8g.PNG%2Fv2.PNG&type=a340")
        imageUrls.add("https://search.pstatic.net/common/?src=http%3A%2F%2Fblogfiles.naver.net%2FMjAyNDAzMDVfMjUw%2FMDAxNzA5NjM0NDc5NjM3.ZTlyfczxfjQL_VKukur1s8AWQd4DeuO8qMnAp6mdmaMg.clBh_0nDh-Ox3uX7-zug3ezWMATU66QBQc4gRxzSpZ4g.JPEG%2FIMG_3512.jpg&type=a340")
        imageUrls.add("https://search.pstatic.net/sunny/?src=https%3A%2F%2Fw7.pngwing.com%2Fpngs%2F885%2F946%2Fpng-transparent-groudon-darkrai-pokedex-pokemon-giratina-pokemon-seafood-fictional-character-crab.png&type=a340")
        imageUrls.add("https://search.pstatic.net/sunny/?src=https%3A%2F%2Fi.namu.wiki%2Fi%2F1Ogotf36_OmhfkNY4gR7Mm_PqdDX8BOEU2qUKhL1SgAnYDsRBbzdS57G4SMqMxypVYDQsP0GSnOoEKD7n3JXhQ.webp&type=a340")
        imageUrls.add("https://search.pstatic.net/common/?src=http%3A%2F%2Fblogfiles.naver.net%2FMjAyNDA0MDlfMTk1%2FMDAxNzEyNjUwMDE1NDk3.vtwekxMeNXbVniVziotwGp-OnxBi1u2LdFSpFgCKiE8g.dP2UWN2IVNTZHmSy4GYROBDgE9YbyhUsw6m7tWIOt0wg.JPEG%2Fbandicam_2024-04-09_17-06-24-112.jpg&type=a340")
        imageUrls.add("https://search.pstatic.net/sunny/?src=https%3A%2F%2Fw7.pngwing.com%2Fpngs%2F866%2F884%2Fpng-transparent-pokemon-x-and-y-pokemon-heartgold-and-soulsilver-pokemon-sun-and-moon-pokemon-crystal-lugia-lugia-pokemon-mammal-vertebrate-cartoon.png&type=a340")
        adapter = RecyclerAdapter(imageUrls)
        binding.recyclerview.adapter = adapter

        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        adapter.setItemClickListener(object : RecyclerAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {
                //여기서 이미지 클릭시 하게될 행동 쓰기
            }
        })

    }
    private fun slideUp(recyclerView: View, toggleButton: MaterialButton) {
        val displayMetrics = resources.displayMetrics
        val px50dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, displayMetrics)

        // 리사이클러뷰 애니메이션 설정
        val recyclerViewAnimator = ObjectAnimator.ofFloat(recyclerView, "translationY", recyclerView.height.toFloat(), 0f)
        // 버튼 애니메이션 설정
        val buttonAnimator = ObjectAnimator.ofFloat(toggleButton, "translationY", recyclerView.height.toFloat() - px50dp, 0f)

        // 애니메이션 세트로 동시에 실행
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(recyclerViewAnimator, buttonAnimator)
        animatorSet.duration = 500

        // 애니메이션 시작 전 가시성 설정
        recyclerView.visibility = View.VISIBLE

        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                toggleButton.setIconResource(R.drawable.arrow_down)
            }
        })
        animatorSet.start()
    }

    private fun slideDown(recyclerView: View, toggleButton: MaterialButton) {
        val displayMetrics = resources.displayMetrics
        val px50dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, displayMetrics)

        // 리사이클러뷰 애니메이션 설정
        val recyclerViewAnimator = ObjectAnimator.ofFloat(recyclerView, "translationY", 0f, recyclerView.height.toFloat())
        // 버튼 애니메이션 설정
        val buttonAnimator = ObjectAnimator.ofFloat(toggleButton, "translationY", 0f, recyclerView.height.toFloat() - px50dp)

        // 애니메이션 세트로 동시에 실행
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(recyclerViewAnimator, buttonAnimator)
        animatorSet.duration = 500

        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                recyclerView.visibility = View.INVISIBLE
                toggleButton.setIconResource(R.drawable.arrow_up)
            }
        })
        animatorSet.start()
    }
}