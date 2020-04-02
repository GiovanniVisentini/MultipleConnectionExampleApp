# Multiple Connection

this app connect to 2 ble devices and show the acceleration

## How to run

 - clone app:

```
    git clone --recurse-submodules https://github.com/GiovanniVisentini/MultipleConnectionExampleApp.git
```

 - Change the `NODE_1_TAG` and `NODE_2_TAG` constants with the mac address of yours device
- install the app on your mobile
- open the mobile settings -> apps -> MultipleConnection -> permission and enable the location permission.
This is needed to have the permission to run the ble scanning. See here as an example to request the permission: [NodeScanActivity](https://github.com/STMicroelectronics/BlueSTSDK_Android/blob/master/BlueSTSDK/src/main/java/com/st/BlueSTSDK/Utils/NodeScanActivity.java)

## Todo
- ask the location permission
- disconnect the nodes, for now you have to manually kill the app.
- handle the case where the activity restart when the node connection is ongoing
