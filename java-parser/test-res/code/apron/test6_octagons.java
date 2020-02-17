{
	int i, j, k;
	i = random;
	j = random;
	k = random;
	assert i > i - 1;
	assert i > 0;//Warn
	if(k>0) {
		while(i < 0) {
			assert i < k;
			i = i + k;
			assert i > k;//Warn
			assert i > i - k;
			if(j>0) {
				k = k + j;
				assert k > j;
				assert j > k; //Warn
			}
		}
	}
	assert i > k; //Warn
	assert k > i; //Warn
}
