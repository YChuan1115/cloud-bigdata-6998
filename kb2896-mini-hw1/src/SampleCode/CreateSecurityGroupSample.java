/*
 * UNI: kb2896
 * Name: Kunal Baweja
 * Subject: COMS E6998 Cloud Computing & Big Data
 */

package SampleCode;
/*
 * Copyright 2010-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

//import java.net.InetAddress;
//import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.IpPermission;

public class CreateSecurityGroupSample {

    /*
     * Before running the code:
     *      Fill in your AWS access credentials in the provided credentials
     *      file template, and be sure to move the file to the default location
     *      where the sample code will load the credentials from.
     *      https://console.aws.amazon.com/iam/home?#security_credential
     *
     * WARNING:
     *      To avoid accidental leakage of your credentials, DO NOT keep
     *      the credentials file in your source directory.
     */

    public static void main(String[] args) {

        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file.
         */
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider("default").getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location, and is in valid format.", e);
        }

        // Create the AmazonEC2Client object so we can call various APIs.
        AmazonEC2 ec2 = new AmazonEC2Client(credentials);
        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        ec2.setRegion(usWest2);

        // Create a new security group.
        try {
            CreateSecurityGroupRequest securityGroupRequest = new CreateSecurityGroupRequest(
                    "my-security-group", "my-security-group");
            CreateSecurityGroupResult result = ec2
                    .createSecurityGroup(securityGroupRequest);
            System.out.println("Security group created: " + result.getGroupId());
        } catch (AmazonServiceException ase) {
            // Likely this means that the group is already created, so ignore.
            System.out.println(ase.getMessage());
        }

        // addr.getHostAddress() returns 127.0.1.1 but we need external ip address of our system
        // I consulted TA and he advised it is ok to hardcode the ip as below because getHostAddress
        // does not works as expected, if the system is behind a NAT. Following is an IP address
        // from Columbia University Network
        String ipAddr = "160.39.250.190/10";
        
//        String ipAddr = "0.0.0.0/0";
//
//         Get the IP of the current host, so that we can limit the Security Group
//         by default to the ip range associated with your subnet.
//        try {
//            InetAddress addr = InetAddress.getLocalHost();
//
//            // Get IP Address
//            ipAddr = addr.getHostAddress()+"/10";
//        } catch (UnknownHostException e) {
//        }

        // Create a range that you would like to populate.
        List<String> ipRanges = Collections.singletonList(ipAddr);

        // Open up port 22 for TCP traffic to the associated IP from above (e.g. ssh traffic).
        IpPermission ipPermission = new IpPermission()
                .withIpProtocol("tcp")
                .withFromPort(new Integer(22))
                .withToPort(new Integer(22))
                .withIpRanges(ipRanges);

        List<IpPermission> ipPermissions = Collections.singletonList(ipPermission);

        try {
            // Authorize the ports to the used.
            AuthorizeSecurityGroupIngressRequest ingressRequest = new AuthorizeSecurityGroupIngressRequest(
                    "my-security-group", ipPermissions);
            ec2.authorizeSecurityGroupIngress(ingressRequest);
            System.out.println(String.format("Ingress port authroized: [%s]",
                    ipPermissions.toString()));
        } catch (AmazonServiceException ase) {
            // Ignore because this likely means the zone has already been authorized.
            System.out.println(ase.getMessage());
        }
    }

}