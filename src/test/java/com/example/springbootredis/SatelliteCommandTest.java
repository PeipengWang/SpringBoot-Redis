//package com.example.springbootredis;
//
//import com.example.springbootredis.entity.Command;
//import com.example.springbootredis.service.SatelliteCommandService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Set;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//class SatelliteCommandTest {
//
//    @Autowired
//    private SatelliteCommandService satelliteCommandService;
//
////    @Test
////    void testMultiSatelliteCommandStorage() {
////        System.out.println("=== 开始测试多卫星指令存储功能 ===");
////
////        // 创建测试数据
////        LocalDateTime now = LocalDateTime.now();
////        LocalDateTime executeTime1 = now.plusMinutes(5); // 5分钟后执行
////        LocalDateTime executeTime2 = now.plusMinutes(10); // 10分钟后执行
////        LocalDateTime executeTime3 = now.plusMinutes(15); // 15分钟后执行
////
////        // 卫星1的指令
////        Command command1 = new Command("cmd_001", "启动引擎", executeTime1, "satellite_001");
////        Command command2 = new Command("cmd_002", "调整轨道", executeTime2, "satellite_001");
////
////        // 卫星2的指令
////        Command command3 = new Command("cmd_003", "姿态调整", executeTime3, "satellite_002");
////        Command command4 = new Command("cmd_004", "通信测试", executeTime1, "satellite_002");
////
////        // 卫星3的指令
////        Command command5 = new Command("cmd_005", "数据采集", executeTime2, "satellite_003");
////
////        // 添加指令到Redis
////        assertTrue(satelliteCommandService.addCommand(command1), "添加卫星1指令1应该成功");
////        assertTrue(satelliteCommandService.addCommand(command2), "添加卫星1指令2应该成功");
////        assertTrue(satelliteCommandService.addCommand(command3), "添加卫星2指令3应该成功");
////        assertTrue(satelliteCommandService.addCommand(command4), "添加卫星2指令4应该成功");
////        assertTrue(satelliteCommandService.addCommand(command5), "添加卫星3指令5应该成功");
////
////        // 验证卫星1的指令
////        List<Command> satellite1Commands = satelliteCommandService.getCommandsBySatellite("satellite_001");
////        assertEquals(2, satellite1Commands.size(), "卫星1应该有2个指令");
////        System.out.println("卫星1的指令数量: " + satellite1Commands.size());
////
////        // 验证卫星2的指令
////        List<Command> satellite2Commands = satelliteCommandService.getCommandsBySatellite("satellite_002");
////        assertEquals(2, satellite2Commands.size(), "卫星2应该有2个指令");
////        System.out.println("卫星2的指令数量: " + satellite2Commands.size());
////
////        // 验证卫星3的指令
////        List<Command> satellite3Commands = satelliteCommandService.getCommandsBySatellite("satellite_003");
////        assertEquals(1, satellite3Commands.size(), "卫星3应该有1个指令");
////        System.out.println("卫星3的指令数量: " + satellite3Commands.size());
////
////        // 验证所有卫星ID
////        Set<String> allSatelliteIds = satelliteCommandService.getAllSatelliteIds();
////        assertEquals(3, allSatelliteIds.size(), "应该有3个卫星");
////        assertTrue(allSatelliteIds.contains("satellite_001"), "应该包含卫星1");
////        assertTrue(allSatelliteIds.contains("satellite_002"), "应该包含卫星2");
////        assertTrue(allSatelliteIds.contains("satellite_003"), "应该包含卫星3");
////
////        System.out.println("所有卫星ID: " + allSatelliteIds);
////
////        // 验证单个指令获取
////        Command retrievedCommand = satelliteCommandService.getCommand("satellite_001", "cmd_001");
////        assertNotNull(retrievedCommand, "应该能获取到指令");
////        assertEquals("启动引擎", retrievedCommand.getContent(), "指令内容应该正确");
////        assertEquals("satellite_001", retrievedCommand.getSatelliteId(), "卫星ID应该正确");
////
////        System.out.println("获取到的指令: " + retrievedCommand);
////
////        // 验证过期时间设置
////        assertNotNull(retrievedCommand.getExpireTime(), "过期时间应该被设置");
////        assertTrue(retrievedCommand.getExpireTime().isAfter(retrievedCommand.getExecuteTime()),
////                "过期时间应该在执行时间之后");
////
////        System.out.println("指令执行时间: " + retrievedCommand.getExecuteTime());
////        System.out.println("指令过期时间: " + retrievedCommand.getExpireTime());
////
////        System.out.println("=== 多卫星指令存储功能测试通过 ===");
////    }
//
//    @Test
//    void testCommandStatusUpdate() {
//        System.out.println("=== 开始测试指令状态更新功能 ===");
//
//        // 创建测试指令
//        LocalDateTime executeTime = LocalDateTime.now().plusMinutes(5);
//        Command command = new Command("cmd_status_test", "状态测试指令", executeTime, "satellite_status_test");
//
//        // 添加指令
//        assertTrue(satelliteCommandService.addCommand(command), "添加指令应该成功");
//
//        // 验证初始状态
//        Command retrievedCommand = satelliteCommandService.getCommand("satellite_status_test", "cmd_status_test");
//        assertEquals("PENDING", retrievedCommand.getStatus(), "初始状态应该是PENDING");
//
//        // 更新状态为执行中
//        assertTrue(satelliteCommandService.updateCommandStatus("satellite_status_test", "cmd_status_test", "EXECUTING"),
//                "更新状态应该成功");
//
//        // 验证状态更新
//        Command updatedCommand = satelliteCommandService.getCommand("satellite_status_test", "cmd_status_test");
//        assertEquals("EXECUTING", updatedCommand.getStatus(), "状态应该更新为EXECUTING");
//
//        // 更新状态为已完成
//        assertTrue(satelliteCommandService.updateCommandStatus("satellite_status_test", "cmd_status_test", "COMPLETED"),
//                "更新状态应该成功");
//
//        // 验证状态更新
//        Command completedCommand = satelliteCommandService.getCommand("satellite_status_test", "cmd_status_test");
//        assertEquals("COMPLETED", completedCommand.getStatus(), "状态应该更新为COMPLETED");
//
//        System.out.println("=== 指令状态更新功能测试通过 ===");
//    }
//
//    @Test
//    void testCommandDeletion() {
//        System.out.println("=== 开始测试指令删除功能 ===");
//
//        // 创建测试指令
//        LocalDateTime executeTime = LocalDateTime.now().plusMinutes(5);
//        Command command1 = new Command("cmd_delete_001", "删除测试指令1", executeTime, "satellite_delete_test");
//        Command command2 = new Command("cmd_delete_002", "删除测试指令2", executeTime, "satellite_delete_test");
//
//        // 添加指令
//        assertTrue(satelliteCommandService.addCommand(command1), "添加指令1应该成功");
//        assertTrue(satelliteCommandService.addCommand(command2), "添加指令2应该成功");
//
//        // 验证指令存在
//        List<Command> commands = satelliteCommandService.getCommandsBySatellite("satellite_delete_test");
//        assertEquals(2, commands.size(), "应该有2个指令");
//
//        // 删除单个指令
//        assertTrue(satelliteCommandService.deleteCommand("satellite_delete_test", "cmd_delete_001"),
//                "删除指令1应该成功");
//
//        // 验证删除结果
//        commands = satelliteCommandService.getCommandsBySatellite("satellite_delete_test");
//        assertEquals(1, commands.size(), "删除后应该只有1个指令");
//
//        // 删除剩余指令
//        assertTrue(satelliteCommandService.deleteCommand("satellite_delete_test", "cmd_delete_002"),
//                "删除指令2应该成功");
//
//        // 验证全部删除
//        commands = satelliteCommandService.getCommandsBySatellite("satellite_delete_test");
//        assertEquals(0, commands.size(), "删除后应该没有指令");
//
//        System.out.println("=== 指令删除功能测试通过 ===");
//    }
//
//    @Test
//    void testExpiredCommandDetection() {
//        System.out.println("=== 开始测试过期指令检测功能 ===");
//
//        // 创建已过期的指令（执行时间在过去，过期时间也在过去）
//        LocalDateTime pastTime = LocalDateTime.now().minusMinutes(10);
//        Command expiredCommand = new Command("cmd_expired", "过期指令", pastTime, "satellite_expired_test");
//
//        // 添加过期指令
//        assertTrue(satelliteCommandService.addCommand(expiredCommand), "添加过期指令应该成功");
//
//        // 验证过期检测
//        Command retrievedCommand = satelliteCommandService.getCommand("satellite_expired_test", "cmd_expired");
//        assertTrue(retrievedCommand.isExpired(), "指令应该被识别为已过期");
//
//        // 验证未到执行时间的指令
//        LocalDateTime futureExecuteTime = LocalDateTime.now().plusMinutes(5);
//        Command futureCommand = new Command("cmd_future", "未来指令", futureExecuteTime, "satellite_future_test");
//
//        assertTrue(satelliteCommandService.addCommand(futureCommand), "添加未来指令应该成功");
//
//        Command futureRetrievedCommand = satelliteCommandService.getCommand("satellite_future_test", "cmd_future");
//        assertFalse(futureRetrievedCommand.isExpired(), "未来指令不应该被识别为已过期");
//        assertFalse(futureRetrievedCommand.isReadyToExecute(), "未来指令不应该准备执行");
//
//        System.out.println("=== 过期指令检测功能测试通过 ===");
//    }
//}
