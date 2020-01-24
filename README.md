# ProM-CLI-hack
Repo holding scripts for interacting with ProM using CLI and testing the invoking of ProM from an external program.

#### What is ProM?
From the [official website](http://www.promtools.org/doku.php) of the ProM project:
> ProM is an extensible framework that supports a wide variety of process mining techniques in the form of plug-ins. It is platform independent as it is implemented in Java, and can be downloaded free of charge.

#### Why do CLI (command-line interface) hack?
Maybe you would want to
> ... run process mining analyses on several logs in batch mode without user interaction.

as quoted from a [blog post](https://dirksmetric.wordpress.com/2015/03/11/tutorial-automating-process-mining-with-proms-command-line-interface/) by Dirk Fahland.

* Or it could be that you want to have some of your own programs working in synergy with ProM but you don't know Java that much.

* Or simply because that a sexy Graphical User Interface is brilliant but is not for everyone.

#### Before start
A fresh download of ProM does not contain even those basic ones (meaning that it could fail in invoking functions like ``open_xes_log_file``). Make sure that you have already run ProM (via GUI) and have solved those basic dependencies (``Runner up packages``) using ProM PM before you get too excited about the CLI hack.

In other words, it seems impossible to get CLI worked from the very beginning, which could pose some challenges if one wants to do all these on a server without GUI and using a fresh download.
(There could be ways to solve those dependencies through CLI but unfortunately I haven't been able to figure them out. My best guess is to do it through an initial script that invokes a series of functions to fetch the first several packages to the local library).

#### Notes
This project is motivated by an issue that I have encountered in my own research, and is largely inspired by Dirk Fahland's blog post (see above) as well as a number of posts from the [ProM forum](https://www.win.tue.nl/promforum/).

These 

Note that this project is for experimental use and does not guarantee any unplanned functionalities, software code compliance or any future maintenance.
