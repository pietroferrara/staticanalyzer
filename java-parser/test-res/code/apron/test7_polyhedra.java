{
	int i, j, k, w;
	i = random;
	j = random;
	k = random;
	w = k + j + i + 1; 
	assert w > k + j + i;
	assert k > w - j; //Warn
	if(k > j) {
		i = k + 1;
		assert i > j;
		assert k > i;//Warn
	}
	if(i > j + k) {
		w = i + 10;
		assert j + k < w;
		assert w >= k;//Warn
	}
}
