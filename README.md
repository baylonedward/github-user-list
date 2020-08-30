# github-user-list
An android app that fetch users list from github.

## UI and  Design
Includes 2 screens:
1. Generic User List Screen with a search view on the appbar.
2. Profile Screen which shows more details of a user, and  edit text field to save notes on the profile.

## Architecture: MVVM

Uses MVVM inspired by Android Jetpack Architecture.

View - observes data and state from the ViewModel.
ViewModel - contains all the processing logic and functions that the view needs.
Model - is serve by the api module through a singleton class GitHubApi.

## Modules:
1. api module - consumes network api, has the local persistence using Android Room, and has Repository class
as the single source of data from two sources (network and local data).  

## Coding Flow
The app and modules uses kotlin coroutines for asynchronous programming.
Libraries like retrofit and android room natively supports flow and suspending functions.