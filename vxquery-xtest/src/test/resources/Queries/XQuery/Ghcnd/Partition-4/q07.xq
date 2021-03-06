(: Licensed to the Apache Software Foundation (ASF) under one
   or more contributor license agreements.  See the NOTICE file
   distributed with this work for additional information
   regarding copyright ownership.  The ASF licenses this file
   to you under the Apache License, Version 2.0 (the
   "License"); you may not use this file except in compliance
   with the License.  You may obtain a copy of the License at
   
     http://www.apache.org/licenses/LICENSE-2.0
   
   Unless required by applicable law or agreed to in writing,
   software distributed under the License is distributed on an
   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
   KIND, either express or implied.  See the License for the
   specific language governing permissions and limitations
   under the License. :)

(: XQuery Self Join Query :)
(: Self join with all stations finding the difference in min and max       :)
(: temperature and get the average.                                        :)
fn:avg(
    let $sensor_collection_min := "ghcnd_quarter_1|ghcnd_quarter_2|ghcnd_quarter_3|ghcnd_quarter_4"
    for $r_min in collection($sensor_collection_min)/dataCollection/data
    
    let $sensor_collection_max := "ghcnd_quarter_1|ghcnd_quarter_2|ghcnd_quarter_3|ghcnd_quarter_4"
    for $r_max in collection($sensor_collection_max)/dataCollection/data
    
    where $r_min/station eq $r_max/station
        and $r_min/date eq $r_max/date
        and $r_min/dataType eq "TMIN"
        and $r_max/dataType eq "TMAX"
    return $r_max/value - $r_min/value
) div 10
