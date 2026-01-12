#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Database Images
docker pull mysql:8.0
docker pull mysql:8.0.29
docker pull oceanbase/oceanbase-ce:latest
docker pull apache/hive:latest

# File System Images
docker pull atmoz/sftp:latest
docker pull fauria/vsftpd:latest

# Testing Utilities
docker pull mockserver/mockserver:5.14.0

echo "All SeaTunnel E2E Docker images pulled successfully."
