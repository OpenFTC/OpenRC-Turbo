#
# Application.mk in RobotCore
#

# Our Vuforia external provider support makes (limited) use of the C++ standard
# library. In reality, we could probably get rid of same if we needed to, but it
# seems to be working, so there's no point. Motiviation for inclusion came from 
# the rather more extensive usage thereof in the PTC-provided samples on which
# we were basing our own work
#
# The world of Andorid native code seems to have an abundance of possible C++
# standard libraries to attempt to choose from. We use the Clang / LLVM version
# as this finally seesm to be in production (it went out of beta as of NDK16).

APP_STL := c++_shared
