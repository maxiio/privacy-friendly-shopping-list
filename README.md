## Overview

### Privacy Friendly App Shopping List

The Privacy Friendly App Shopping List is an Android application that does not require any permissions from the user in order to be installed. With this app users will be able to manage their shopping lists. Basically users will be able to add, edit and remove lists. A shopping list contains products which can be added, edited and removed as well.

## Motivation

Nowadays there are many apps that require many or all permissions available in Android in order to be installed. However these apps do not always need all of the permissions they ask for. With this project we want to offer an app where the user can be sure that private information such as contacts, location, identity etc., are not being used by the application.

## Versioning

Version 0.1 (Beta). Features:
- Lists can be added, edited and removed
- Lists can be sorted by name or priority
- Products can be added, edited and removed from lists
- Products can be sorted by name, store, quantity, price or category
- Statistics (total amount) can be visualized in a chart
- Statistics' chart can be filtered by range and the results can be showed grouped by month, week, day, category, store or product.
- Statistics can be turned on/off inside a list
- Statistics option can per default be set to be turned on/off in the settings
- Statistics can be fully deleted from the settings menu
- Currency can be set by the user in the setings menu (up to 3 characters)

## Installation

The app can be installed by using IntelliJ or Android Studio.

1. Download the source code: $ git clone https://github.com/SecUSo/privacy-friendly-shopping-list.git
2. Add the local.properties and the build.properties file to the root by removing the extension ".copy" from "local.properties.copy" and "build.gradle.copy"
3. Open the IDE of your choice (recomened: IntelliJ or Android Studio)
4. Connect the mobile device to the computer
5. Setup a Run Configuration "Android Application" and select "app" under Module
6. Click run and select "Choose a running device" from the "Device Chooser" Dialog


## License

Copyright 2016 by Grebiel J. Ifill B. and Christian König

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

## Contributors
Prof. Dr. Melanie Volkamer,
Christopher Beckmann,
Karola Marky,
Peter Mayer