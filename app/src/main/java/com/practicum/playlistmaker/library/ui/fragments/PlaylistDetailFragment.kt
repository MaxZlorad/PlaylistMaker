package com.practicum.playlistmaker.library.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentPlaylistDetailBinding
import com.practicum.playlistmaker.library.domain.models.PlaylistDetailState
import com.practicum.playlistmaker.library.ui.view_model.PlaylistDetailViewModel
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.search.ui.track.TrackAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import android.app.AlertDialog
import android.content.Intent
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.practicum.playlistmaker.main.utils.getFormattedTime


// Fragment для отображения информации о плейлисте и списка его треков
class PlaylistDetailFragment : Fragment() {

    // ViewBinding для доступа к элементам UI
    private var _binding: FragmentPlaylistDetailBinding? = null
    private val binding get() = _binding!!

    // ViewModel для управления данными экрана (через inject)
    private val viewModel: PlaylistDetailViewModel by viewModel()  // Посылаем в Koin

    // Адаптер для списка треков с поддержкой длинного нажатия
    private val trackAdapter = TrackAdapter(
        tracks = emptyList(),
        onItemClick = { track -> openPlayer(track) },
        onItemLongClick = { track -> showDeleteDialog(track)
            true } // событие обработано
    )

    // BottomSheetBehavior для меню
    private lateinit var menuBottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    // Текущее состояние плейлиста (для шеринга и удаления)
    private var currentPlaylist: com.practicum.playlistmaker.library.domain.models.Playlist? = null
    private var currentTracks: List<Track> = emptyList()

    // Защита от двойных кликов
    private var lastClickTime = 0L
    private val CLICK_DEBOUNCE_DELAY = 1000L // 1 секунда

    // Диалог подтверждения удаления трека
    private fun showDeleteDialog(track: Track) {
        // Показываем затемнение
        binding.overlay.visibility = View.VISIBLE

        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialog)
            .setMessage(R.string.delete_track_dialog_title)
            .setNegativeButton(R.string.dialog_no, null)
            .setPositiveButton(R.string.dialog_yes, null)
            .setOnDismissListener {
                //Скрываем overlay при любом закрытии
                binding.overlay.visibility = View.GONE
            }
            .create()

        dialog.show()

        // Настраиваем кнопки после show()
        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.apply {
            setTextColor(resources.getColor(R.color.blue, null)) // Синий цвет
            setOnClickListener {
                viewModel.removeTrack(track.trackId)
                dialog.dismiss()
            }
        }

        dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)?.setOnClickListener {
            dialog.dismiss()
        }
    }

    // ID плейлиста, переданный через аргументы
    private var playlistId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Получаем ID плейлиста из аргументов
        playlistId = arguments?.getInt(ARGS_PLAYLIST_ID) ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Скрыть навигацию через root

        // Настраиваем UI элементы
        setupToolbar()
        setupRecyclerView()
        setupButtons()
        setupMenuBottomSheet()

        // Подписываемся на состояние из ViewModel
        observeViewModel()

        // Загружаем данные плейлиста
        viewModel.loadPlaylist(playlistId)
    }

    // Настройка Toolbar с кнопкой "Назад"
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    // Настройка RecyclerView для отображения списка треков
    private fun setupRecyclerView() {
        binding.tracksRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = trackAdapter
        }
    }

    // Настройка кнопок "Поделиться" и "Меню"
    private fun setupButtons() {
        // Кнопка "Поделиться"
        binding.shareButton.setOnClickListener {
            if (debounceClick()) {
                sharePlaylist()
            }
        }

        binding.menuButton.setOnClickListener {
            if (debounceClick()) {
                showMenuBottomSheet()
            }
        }
    }

    // Подписка на состояние ViewModel
    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PlaylistDetailState.Loading -> showLoading()
                is PlaylistDetailState.Content -> showContent(state)
                is PlaylistDetailState.Empty -> showEmpty()
                is PlaylistDetailState.Error -> showError()
            }
        }
    }

    // Отображение состояния загрузки
    private fun showLoading() {
        // Показать ProgressBar (если будет если нужен)
    }


    // Отображение пустого плейлиста (нет треков)
    private fun showEmpty() {
        // Показать заглушку "В плейлисте пока нет треков"
    }

    // Отображение ошибки (плейлист не найден)
    private fun showError() {
        // Показать сообщение "Плейлист не найден"
        findNavController().navigateUp()
    }

    // Открывает PlayerFragment для воспроизведения трека
    private fun openPlayer(track: Track) {
        // используем существующий action из navigation_graph
        findNavController().navigate(
            R.id.action_playlistDetailFragment_to_playerFragment,
            bundleOf("track" to track)
        )
    }

    // BS Меню

    private fun setupMenuBottomSheet() {
        val bottomSheetContainer = binding.menuBottomSheet
        menuBottomSheetBehavior = BottomSheetBehavior.from(bottomSheetContainer).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        // Затемнение при открытии/закрытии меню
        menuBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.overlay.visibility = View.GONE
                    }
                    else -> {
                        binding.overlay.visibility = View.VISIBLE
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        // Скрыть меню при клике на overlay
        binding.overlay.setOnClickListener {
            if (debounceClick()) {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
        }

        // Обработчики пунктов меню
        binding.menuShare.setOnClickListener {
            if (debounceClick()) {
                menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                sharePlaylist()
            }
        }

        binding.menuEdit.setOnClickListener {
            if (debounceClick()) {
                menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                // Переход к редактированию плейлиста
                findNavController().navigate(
                    R.id.action_playlistDetailFragment_to_newPlaylistFragment,
                    bundleOf("playlistId" to playlistId)
                )
            }
        }

        binding.menuDelete.setOnClickListener {
            if (debounceClick()) {
                menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                showDeletePlaylistDialog()
            }
        }
    }

    private fun showMenuBottomSheet() {
        // Обновляем превью плейлиста в меню
        currentPlaylist?.let { playlist ->
            binding.menuPlaylistName.text = playlist.name
            binding.menuPlaylistTrackCount.text = resources.getQuantityString(
                R.plurals.track_count,
                playlist.trackCount,
                playlist.trackCount
            )

            // Загружаем обложку
            val coverFile = playlist.coverImagePath?.let { File(it) }
            val cornerRadius = (2 * resources.displayMetrics.density).toInt()

            if (coverFile != null && coverFile.exists()) {
                Glide.with(this)
                    .load(coverFile)
                    .placeholder(R.drawable.placeholder_album)
                    .transform(RoundedCorners(cornerRadius))
                    .into(binding.menuPlaylistCover)
            } else {
                binding.menuPlaylistCover.setImageResource(R.drawable.placeholder_album)
            }
        }

        menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    // Раздел поделиться

    private fun sharePlaylist() {
        if (currentTracks.isEmpty()) {
            Toast.makeText(
                requireContext(),
                R.string.empty_playlist_share_message,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val shareText = buildShareText()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share_playlist)))
    }

    private fun buildShareText(): String {
        val playlist = currentPlaylist ?: return ""
        val builder = StringBuilder()

        // Название плейлиста
        builder.append(playlist.name).append("\n")

        // Описание (если есть)
        if (!playlist.description.isNullOrEmpty()) {
            builder.append(playlist.description).append("\n")
        }

        // Количество треков
        builder.append(
            resources.getQuantityString(
                R.plurals.track_count,
                playlist.trackCount,
                playlist.trackCount
            )
        ).append("\n")

        // Список треков
        currentTracks.forEachIndexed { index, track ->
            builder.append("${index + 1}. ${track.artistName} - " +
                    "${track.trackName} (${getFormattedTime(track.trackTimeMillis)})\n")
        }

        return builder.toString()
    }

    // Раздел "Удаление плейлиста"
    private fun showDeletePlaylistDialog() {
        binding.overlay.visibility = View.VISIBLE

        // Формируем сообщение с названием плейлиста
        val message = getString(
            R.string.delete_playlist_dialog_message, currentPlaylist?.name ?: "")

        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialog)
            .setMessage(message)
            .setNegativeButton(R.string.dialog_no, null)
            .setPositiveButton(R.string.dialog_yes, null)
            .setOnDismissListener {
                binding.overlay.visibility = View.GONE
            }
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
            setTextColor(resources.getColor(R.color.blue, null))
            setOnClickListener {
                currentPlaylist?.let { playlist ->
                    viewModel.deletePlaylist(playlist.playlistId)
                }
                dialog.dismiss()
                findNavController().navigateUp() // Возврат на Медиатеку
            }
        }

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setOnClickListener {
            dialog.dismiss()
        }
    }

    // Защита от двойного клика
    private fun debounceClick(): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime < CLICK_DEBOUNCE_DELAY) {
            return false
        }
        lastClickTime = currentTime
        return true
    }

    private fun showContent(state: PlaylistDetailState.Content) {
        val playlist = state.playlist
        val tracks = state.tracks

        // Загружаем обложку плейлиста
        val coverFile = playlist.coverImagePath?.let { File(it) }

        if (coverFile != null && coverFile.exists()) {
            // Картинка без закругления
            binding.playlistCover.background = null
            binding.playlistCover.scaleType = ImageView.ScaleType.CENTER_CROP
            Glide.with(this)
                .load(coverFile)
                .placeholder(R.drawable.placeholder_album)
                .into(binding.playlistCover)
        } else {
            // С закруглением 8dp как в макете Figma
            binding.playlistCover.setBackgroundResource(R.drawable.rounded_corners)
            binding.playlistCover.clipToOutline = true
            binding.playlistCover.scaleType = ImageView.ScaleType.CENTER
            binding.playlistCover.setImageResource(R.drawable.placeholder_album)
        }

        currentPlaylist = playlist
        currentTracks = tracks

        // Название плейлиста
        binding.playlistName.text = playlist.name

        // Описание (показываем, если есть)
        if (playlist.description.isNullOrEmpty()) {
            binding.playlistDescription.visibility = View.GONE
        } else {
            binding.playlistDescription.visibility = View.VISIBLE
            binding.playlistDescription.text = playlist.description
        }

        // Длительность плейлиста
        binding.playlistDuration.text = viewModel.formatDuration(playlist.totalDuration)

        // Количество треков
        binding.playlistTrackCount.text = resources.getQuantityString(
            R.plurals.track_count,
            playlist.trackCount,
            playlist.trackCount
        )

        // Обновляем список треков
        trackAdapter.updateTracks(state.tracks)

        // Показываем/скрываем сообщение "нет треков" (Формат в задании не указан)
        if (tracks.isEmpty()) {
            binding.emptyTracksMessage.visibility = View.VISIBLE
            binding.tracksRecyclerView.visibility = View.GONE
        } else {
            binding.emptyTracksMessage.visibility = View.GONE
            binding.tracksRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Восстанавливаем навигацию (root)

        _binding = null
    }

    companion object {
        private const val ARGS_PLAYLIST_ID = "playlist_id"

        // Создаёт Bundle с аргументами для передачи ID плейлиста
        fun createArgs(playlistId: Int) = bundleOf(ARGS_PLAYLIST_ID to playlistId)
    }
}
