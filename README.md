# Remote Desktop Connection Solutions for Businesses

## Introduction

* A remote desktop connection is a program or system that enables one user to connect to a desktop computer from a totally different location.
* Remote desktop connections work by capturing the target device’s screen, mouse, and keyboard input drivers. It then transmits them to the other device, allowing the user to view, operate, and control the device.
* The classic application for remote desktop technology is IT support. Using remote desktop connections, IT professionals are able to take control of remote workers’ devices and troubleshoot issues without having to laboriously talk them through each step. However, there are many more applications and benefits of remote desktop connections for businesses like: tracking employees app history, send secure messages..

## How the system works

### System overview:

1. Server: The company will setup a server to handle connection from admins or employees, forward message from admins to employees, store the app history of employee, set up the session for remote desktop..
2. Admin client: The company will give admins a software that can connect to server and do what admin can do like: tracking employee app history, send messages, remote desktop control..
3. Employee client: The company will give employee the device(laptop, pc..) that have a service pre-installed that report app history, tracking and receive messages from server, admin.
4. System admin Cli: The company will give system admin a cli that can generate RSA public and private keys, this key is require to active the client admin or client employee software.

### How it works?

* Features like send messages, view/report app history.. work like TCP client-server model. When admin want to remote desktop control employee computer, server will set up the session and then admin and employee will talk directly like UDP P2P model.
* All messages are encrypted by AES, the AES key will be generate new each connection and pass by RSA, RSA key will generate when active client admin or employee software and pass physically. RSA key will get renew every year.
* More info at: https://docs.google.com/presentation/d/1KCX2Au1Bn7ueZE2dv-cOY465q07Hc1uB/edit?usp=drive_link&ouid=111990615038066897054&rtpof=true&sd=true or https://drive.google.com/file/d/1O1kTiis7BRwTNS1pN6wk_43cT_fNXby0/view?usp=drive_link

## Installation

Will update later. See more on: https://drive.google.com/file/d/1O1kTiis7BRwTNS1pN6wk_43cT_fNXby0/view?usp=drive_link

## Will do if have time

* Make algorithm to find differences between two frames, then only send the differences
* Update to newer image compression algorithm like AVIF, HEIC..
* Optimize more by native library, use SIMD







