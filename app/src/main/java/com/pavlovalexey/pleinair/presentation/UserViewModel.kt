package com.pavlovalexey.pleinair.presentation

@HiltViewModel
class UserViewModel @Inject constructor(
    private val getUsersUseCase: GetUsersUseCase
) : ViewModel() {
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    init {
        viewModelScope.launch {
            val usersList = getUsersUseCase.execute()
            _users.value = usersList
        }
    }
}