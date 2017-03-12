<?php
require_once('./conn.php');

if($_POST['username'] != "" && $_POST['target'] != "") 
{
	$sql = "select * from `user` where username='$_POST[username]' or username='$_POST[target]'";
	$query = mysqli_query($connect, $sql);
	
	$rows = mysqli_num_rows($query);
	if($rows == 2) 
	{
		$list1 = mysqli_fetch_array($query);
		$list2 = mysqli_fetch_array($query);
		$userid1 = $list1['ID'];
		$userid2 = $list2['ID'];
		if($userid1 > $userid2) 
		{
			$t = $userid1;
			$userid1 = $userid2;
			$userid2 = $t;
		}
		
		$sql = "select * from friend where userid1 = $userid1 and userid2 = $userid2";
		$query = mysqli_query($connect, $sql);
		$rows = mysqli_num_rows($query);
		if ($rows == 0) {
			$sql = "insert into friend (userid1, userid2) values($userid1, $userid2)";
			$query = mysqli_query($connect, $sql);
			$result = array("status"=>"success");
			echo json_encode($result);
		}
		else {
			$result = array("status"=>"error");
			echo json_encode($result);
		}
	}
	else
	{
		$result = array("status"=>"error");
		echo json_encode($result);
	}
}
?>
