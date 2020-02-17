{
	int i, j, k;
	i = random;
	j = random;
	k = random;
	assert j < i; //Warn
	i = j + 1;
	assert j < i;
	if(k > j + 100) {
		assert k > j + 100;
		assert k > j + 101;//Warn
	}
	else k = i + 100;
	assert k > i + 99;
	assert i > k;//Warn
	
}
