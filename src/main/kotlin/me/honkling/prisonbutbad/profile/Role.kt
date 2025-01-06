package me.honkling.prisonbutbad.profile

enum class Role(val isAuthority: Boolean) {
    Prisoner(false),
    Escapee(false),
    Nurse(true),
    Guard(true),
    RiotGuard(true),
    Warden(true)
}