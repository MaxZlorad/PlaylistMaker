package com.practicum.playlistmaker.library.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentNewPlaylistBinding
import com.practicum.playlistmaker.library.ui.view_model.NewPlaylistViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream
import androidx.activity.OnBackPressedCallback

// Fragment для создания нового плейлиста
class NewPlaylistFragment : Fragment() {

    private var _binding: FragmentNewPlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NewPlaylistViewModel by viewModel()

    // Photo Picker для выбора изображения
    private val pickMedia = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.setCoverImage(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar() // Кнопка "Назад" в Toolbar
        setupBackPressedHandler() // Обработка системной кнопки Back
        setupCoverImagePicker() // Выбор изображения
        setupTextFields() // Поля ввода
        observeViewModel() // Подписка на LiveData
        setupCreateButton() // Кнопка "Создать"
    }

    // Настройка обработки системной кнопки "Назад" (Back)
    private fun setupBackPressedHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, // Важно! Callback будет жить только пока живет Fragment
            object : OnBackPressedCallback(true) { // true = callback активен
                override fun handleOnBackPressed() {
                    // Проверяем, есть ли несохранённые данные
                    if (viewModel.hasUnsavedData()) {
                        // Если есть — показываем диалог
                        showExitConfirmationDialog()
                    } else {
                        // Если нет — просто выходим
                        isEnabled = false // Отключаем callback, чтобы не зациклить
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )
    }

    // Показать диалог подтверждения выхода
    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.exit_confirmation_title) // "Завершить создание плейлиста?"
            .setMessage(R.string.exit_confirmation_message) // "Все несохраненные данные будут потеряны"
            .setPositiveButton(R.string.finish) { _, _ -> // "Завершить"
                // Пользователь подтвердил выход
                findNavController().navigateUp()
            }
            .setNegativeButton(R.string.cancel, null) // "Отмена" — просто закрываем диалог
            .show()
    }

    // Настройка Toolbar с кнопкой "Назад"
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            handleBackPressed()
        }
    }

    // Настройка выбора изображения обложки
    private fun setupCoverImagePicker() {
        binding.coverImageContainer.setOnClickListener {
            pickMedia.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }

    // Настройка полей ввода текста (Слушаем изменения и обновляем ViewModel)
    private fun setupTextFields() {
        // Поле названия
        binding.nameEditText.doAfterTextChanged { text ->
            viewModel.updatePlaylistName(text.toString())
        }

        // Поле описания
        binding.descriptionEditText.doAfterTextChanged { text ->
            viewModel.updatePlaylistDescription(text.toString())
        }
    }

    // Подписка на изменения в ViewModel
    private fun observeViewModel() {
        // Наблюдаем за выбранным изображением обложки
        viewModel.coverImageUri.observe(viewLifecycleOwner) { uri ->
            if (uri != null) {
                // Показываем выбранное изображение
                binding.coverImageView.visibility = View.VISIBLE
                binding.cameraIcon.visibility = View.GONE

                Glide.with(this)
                    .load(uri)
                    .centerCrop()
                    .into(binding.coverImageView)
            } else {
                // Показываем placeholder
                binding.coverImageView.visibility = View.GONE
                binding.cameraIcon.visibility = View.VISIBLE
            }
        }

        // Наблюдаем за состоянием кнопки "Создать"
        viewModel.isCreateButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            binding.createButton.isEnabled = isEnabled
        }

        // Наблюдаем за событием успешного создания плейлиста
        viewModel.playlistCreated.observe(viewLifecycleOwner) { playlistName ->
            // Показываем Toast
            Toast.makeText(
                requireContext(),
                getString(R.string.playlist_created, playlistName),
                Toast.LENGTH_SHORT
            ).show()

            // Возвращаемся на предыдущий экран
            findNavController().navigateUp()
        }
    }

    // Настройка кнопки "Создать"
    private fun setupCreateButton() {
        binding.createButton.setOnClickListener {
            viewModel.createPlaylist { uri ->
                saveImageToPrivateStorage(uri)
            }
        }
    }

    // Сохранить изображение в private storage приложения
    private fun saveImageToPrivateStorage(uri: Uri): String? {
        return try {
            // Создаём папку для обложек плейлистов
            val playlistCoversDir = File(
                requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "playlist_covers"
            )

            if (!playlistCoversDir.exists()) {
                playlistCoversDir.mkdirs()
            }

            // Создаём уникальное имя файла
            val fileName = "cover_${System.currentTimeMillis()}.jpg"
            val file = File(playlistCoversDir, fileName)

            // Копируем изображение в private storage
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            file.absolutePath // Возвращаем путь к файлу

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Обработка нажатия кнопки "Назад"
    private fun handleBackPressed() {
        if (viewModel.hasUnsavedData()) {
            showExitConfirmationDialog()
        } else {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
