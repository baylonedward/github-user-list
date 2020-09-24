# github-user-list
An android app that fetch users list from github.

## UI and  Design
Includes 2 screens:
1. Generic User List Screen with a search view on the appbar.
2. Profile Screen which shows more details of a user,
and edit text field to save notes on the profile.

## Architecture: MVVM

Uses MVVM inspired by Android Jetpack Architecture.

View - observes data and state from the ViewModel.
ViewModel - contains all the processing logic and functions that the view needs.
Model - is serve by the api module through a singleton class GitHubApi.

### Navigation

I used the Single Activity architecture and Navigation Component from
Jetpack's architecture component to implement screen navigations.

### Programming

I implemented the observer pattern using kotlin coroutines' Flow.

Used kotlin's suspending function feature for easier implementation
of threading and asynchronous task.

## Persistence

I used Android Room to have easier interface to Android's sqlite
for local data storage.

I implemented repository pattern to centralize the data from both local
and network sources.

I implemented one of the most common data access strategy to access data:

1. On query return observable local data.
2. Invoke API network call.
3. Save response to local storage.
4. Updates on the local storage are automatically available for the subscribers.

Also used a helper data class (Resource) to encapsulate data according
to their states which makes it easier for views to consume.

### API

Github api was used.

Consumed using Retrofit Network Library and mapped data using GSON.

Created a service with 2 functions:

1. getUsersSince(id: Int) - Fetch a list of data starting exclusive of the id
parameter.

2. getUserProfile(userName) - Fetch profile for the given username.

## Modules:
1. api module - consumes network api, has the local persistence using Android Room, and has Repository class
as the single source of data from two sources (network and local data).