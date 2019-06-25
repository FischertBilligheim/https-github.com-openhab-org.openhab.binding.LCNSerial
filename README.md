# LCNSerial Binding


This binding provides support for an LCN-Bussystem - with a serial interface LCN-PK (or maybe LCN- PKU? )
The binding ist tested on a Windows10-PC and a Raspberry-Pi using a LCN-PK Modul with a USB-Serial cable. 
The serial-Port can be configured with the Thing configuration.

![LCN Bus](./lcn_bus.jpg)

In this first release, all Things (LCN-Modules) must use the same COM-Port.

## Supported Things

The binding defines a single thing type called `lcnActor`.

An lcnActor requires the single configuration parameter `port`, which specifies the serial port that should be used. 


### Configuration

The configuration for the `LCNSerial` consists of the following parameters:

| Parameter            | Type    | Description                                                                                         |
|----------------------|---------|-----------------------------------------------------------------------------------------------------|
| port                 | String  | The serial port where the LCN-PK is connected to.   (e.g. Linux: `/dev/ttyUSB1`, Windows: `COM2`)   |
| lcn_id               |  int    | LCN-Address of the LCN-Module                                                                       |
| Shutter1_enabled     | boolean | Does the LCN-Module has a Relais-Modul connected with Shutter1 connected?                           |
| Shutter1_drivetime   | int     | Time in seconds the Rollershutter needs to close the window/door 100%                               | 
| Shutter1_overalltime | int     | Time in seconds the Rollershutter needs to come to the end-position                                 | 
| Shutter2_enabled     | boolean | Does the LCN-Module has a Relais-Modul connected with Shutter1 connected?                           |
| Shutter2_drivetime   | int     | Time in seconds the Rollershutter needs to close the window/door 100%                               | 
| Shutter2_overalltime | int     | Time in seconds the Rollershutter needs to come to the end-position                                 | 
| Shutter3_enabled     | boolean | Does the LCN-Module has a Relais-Modul connected with Shutter1 connected?                           |
| Shutter3_drivetime   | int     | Time in seconds the Rollershutter needs to close the window/door 100%                               | 
| Shutter3_overalltime | int     | Time in seconds the Rollershutter needs to come to the end-position                                 |
| Shutter4_enabled     | boolean | Does the LCN-Module has a Relais-Modul connected with Shutter1 connected?                           |
| Shutter4_drivetime   | int     | Time in seconds the Rollershutter needs to close the window/door 100%                               | 
| Shutter4_overalltime | int     | Time in seconds the Rollershutter needs to come to the end-position                                 | 


## Channels

| Channel       | Type          | Description                |
| ------------- |---------------|--------------------------- |
| Output1       | Dimmer        |              tbd           |
| Output2       | Dimmer        |              tbd           |
| Relais_Bit1   | Switch        |              tbd           |
| Relais_Bit2   | Switch        |              tbd           |
| Relais_Bit3   | Switch        |              tbd           |
| Relais_Bit4   | Switch        |              tbd           |
| Relais_Bit5   | Switch        |              tbd           |
| Relais_Bit6   | Switch        |              tbd           |
| Relais_Bit7   | Switch        |              tbd           |
| Relais_Bit8   | Switch        |              tbd           |
| Shutter1      | Rollershutter |              tbd           |
| Shutter2      | Rollershutter |              tbd           |
| Shutter3      | Rollershutter |              tbd           |
| Shutter4      | Rollershutter |              tbd           |

## Configuration Paper-UI
![Paper-UI1](./Configuration1.jpg)
![Paper-UI2](./Configuration2.jpg)



## Usage of rollershutters
![Relias-Modul](./LCN-R8H.jpg)

## Full Example

demo.things:

```
TBD
```

demo.items:

```
TBD
```

_Note:_ This is a trigger channel, so you will most likely bind a second (state) channel to your item, which will control your physical light, so you might end up with the following, if you want to use your button with a Hue bulb:

```
TBD
```

demo.sitemap:

```
sitemap demo label="Main Menu"
{
    Frame {
        TBD
    }
}
```
