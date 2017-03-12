<?php
require_once('./conn.php');

if(isset($_POST['username']) && $_POST['username'] != "")
{
	$username = $_POST['username'];
	$sql = "select id from user where username = '$username'";
	
	$query = mysqli_query($connect, $sql);
	$list = mysqli_fetch_array($query);
	$userid = $list['id'];
	
	$sql2 = "select * from friend where userid1 = $userid or userid2 = $userid";
	$query = mysqli_query($connect, $sql2);
	$friendList = array();
	while($list = mysqli_fetch_array($query)) {
		$targetUserid = ($list['userid1'] == $userid ? $list['userid2'] : $list['userid1']);
		$sql = "select username from user where id = $targetUserid";
		$query2 = mysqli_query($connect, $sql);
		$list2 = mysqli_fetch_array($query2);
		$friendList[] = $list2['username'];
	}
	
	echo json_encode($friendList);
}
	
?>
